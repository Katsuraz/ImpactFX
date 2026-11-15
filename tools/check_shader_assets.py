#!/usr/bin/env python3
"""Statically check phase 4's scene-effect assets and spatial grid. Not a GPU runtime test."""
import json
from pathlib import Path

ASSETS = Path(__file__).resolve().parents[1] / "src/main/resources/assets/impactfx"
root = ASSETS / "post_effect"
shader = (ASSETS / "shaders/post/scene_impact.fsh").read_text(encoding="utf-8")
for token in ("uniform ImpactFXConfig", "vec2 Origin", "vec2 origin = clamp(Origin",
              "WaveRadius", "Distortion", "Punch", "InSampler"):
    assert token in shader, token

NORMAL = ("manga", "anime", "ink", "retro", "cinematic")
LEGACY = ("snap", "strike", "shock", "echo", "clean")
SPATIAL = ("snap", "strike", "shock0", "shock1", "shock2", "echo", "clean")
files = []

for name in NORMAL:
    for tier in ("soft", "strong"):
        files.append((root / f"scene_{name}_{tier}.json", NORMAL.index(name), .5, .5))
for name in LEGACY:
    for tier in ("soft", "strong"):
        files.append((root / f"scene_overdrive_{name}_{tier}.json", 5, .5, .5))
for name in SPATIAL:
    for tier in ("soft", "strong"):
        for gx in range(5):
            for gy in range(3):
                files.append((root / f"scene_overdrive_{name}_{tier}_x{gx}_y{gy}.json",
                             5, round(.1+.2*gx,5), round(5/6-gy/3,5)))

for filename,style,x,y in files:
    data = json.loads(filename.read_text(encoding="utf-8"))
    assert "swap" in data["targets"],filename
    assert len(data["passes"]) == 2,filename
    a,b=data["passes"]
    assert a["fragment_shader"]=="impactfx:post/scene_impact",filename
    assert a["inputs"]==[{"sampler_name":"In","target":"minecraft:main"}],filename
    assert a["output"]=="swap",filename
    assert b["output"]=="minecraft:main" and b["inputs"][0]["target"]=="swap",filename
    uniforms={u["name"]:u for u in a["uniforms"]["ImpactFXConfig"]}
    assert set(uniforms)=={"Strength","Style","WaveRadius","Distortion","Punch","Origin"},filename
    assert uniforms["Style"]["value"]==style,filename
    assert 0 < uniforms["Strength"]["value"]<=1,filename
    assert 0<=uniforms["WaveRadius"]["value"]<=.60,filename
    assert 0<=uniforms["Distortion"]["value"]<=.028,filename
    assert 0<=uniforms["Punch"]["value"]<=.08,filename
    assert uniforms["Origin"]["type"]=="vec2" and len(uniforms["Origin"]["value"])==2,filename
    assert abs(uniforms["Origin"]["value"][0]-x)<.001,filename
    assert abs(uniforms["Origin"]["value"][1]-y)<.001,filename
    if "_clean_" in filename.name:
        assert uniforms["Punch"]["value"]==uniforms["Distortion"]["value"]==0,filename
    if "_soft" in filename.name and "overdrive" in filename.name:
        assert uniforms["Punch"]["value"]<=.025,filename

print(f"PASS: ImpactFX alpha 4 {len(files)} GPU post-effect chains and spatial coordinates")
