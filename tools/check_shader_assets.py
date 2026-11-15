#!/usr/bin/env python3
"""Check Phase 3 shader names, chain references, bounded visual distortion (not GPU execution)."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/impactfx"
STYLES = ("manga", "anime", "ink", "retro", "cinematic")
OVERDRIVE = ("snap", "strike", "shock", "echo", "clean")
TIERS = ("soft", "strong")

shader = ASSETS / "shaders/post/scene_impact.fsh"
source = shader.read_text(encoding="utf-8")
for token in ("#version 330", "layout(location = 0) in vec2 texCoord",
              "layout(location = 0) out vec4 fragColor", "uniform ImpactFXConfig",
              "InSampler", "WaveRadius", "Distortion", "Punch"):
    assert token in source, f"Missing shader declaration: {token}"

files_checked = 0
for name in STYLES + tuple("overdrive_" + stage for stage in OVERDRIVE):
    for tier in TIERS:
        file = ASSETS / "post_effect" / f"scene_{name}_{tier}.json"
        data = json.loads(file.read_text(encoding="utf-8"))
        assert "swap" in data["targets"], file
        assert len(data["passes"]) == 2, file
        first, second = data["passes"]
        assert first["vertex_shader"] == "minecraft:core/screenquad", file
        assert first["fragment_shader"] == "impactfx:post/scene_impact", file
        assert first["inputs"] == [{"sampler_name": "In", "target": "minecraft:main"}], file
        assert first["output"] == "swap", file
        cfg = {u["name"]: u for u in first["uniforms"]["ImpactFXConfig"]}
        assert set(cfg) == {"Strength", "Style", "WaveRadius", "Distortion", "Punch"}, file
        assert all(uniform["type"] == "float" for uniform in cfg.values()), file
        assert 0 < cfg["Strength"]["value"] <= 1, file
        assert 0 <= cfg["WaveRadius"]["value"] <= .60, file
        assert 0 <= cfg["Distortion"]["value"] <= .028, file
        assert 0 <= cfg["Punch"]["value"] <= .08, file
        expected_style = 5 if name.startswith("overdrive") else STYLES.index(name)
        assert cfg["Style"]["value"] == expected_style, file
        if name == "overdrive_clean":
            assert cfg["Distortion"]["value"] == cfg["Punch"]["value"] == 0, file
        if tier == "soft" and name.startswith("overdrive"):
            assert cfg["Distortion"]["value"] < .015, file
            assert cfg["Punch"]["value"] < .025, file
        assert second["fragment_shader"] == "minecraft:post/blit", file
        assert second["inputs"] == [{"sampler_name": "In", "target": "swap"}], file
        assert second["output"] == "minecraft:main", file
        files_checked += 1

print(f"PASS: ImpactFX Phase 3 shader and {files_checked} post-effect JSON definitions")
