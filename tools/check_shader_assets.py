#!/usr/bin/env python3
"""Static check of Minecraft 26.3 post-effect resources (no GPU required)."""
import json
from pathlib import Path

ASSETS = Path(__file__).resolve().parents[1] / "src/main/resources/assets/impactfx"
STYLES = ("manga", "anime", "ink", "retro", "cinematic")
TIERS = ("soft", "strong")

shader = ASSETS / "shaders/post/scene_impact.fsh"
assert shader.is_file(), f"Missing shader: {shader}"
source = shader.read_text(encoding="utf-8")
for token in ("#version 330", "layout(location = 0) in vec2 texCoord",
              "layout(location = 0) out vec4 fragColor",
              "uniform ImpactFXConfig", "InSampler"):
    assert token in source, f"Missing shader declaration: {token}"

for index, style in enumerate(STYLES):
    for tier in TIERS:
        file = ASSETS / "post_effect" / f"scene_{style}_{tier}.json"
        data = json.loads(file.read_text(encoding="utf-8"))
        assert "swap" in data["targets"], file
        assert len(data["passes"]) == 2, file
        first, second = data["passes"]
        assert first["vertex_shader"] == "minecraft:core/screenquad", file
        assert first["fragment_shader"] == "impactfx:post/scene_impact", file
        assert first["inputs"] == [{"sampler_name": "In", "target": "minecraft:main"}], file
        assert first["output"] == "swap", file
        config = {u["name"]: u for u in first["uniforms"]["ImpactFXConfig"]}
        assert config["Style"]["type"] == "float" and config["Style"]["value"] == index, file
        assert config["Strength"]["type"] == "float", file
        assert 0 < config["Strength"]["value"] <= 1, file
        assert second["fragment_shader"] == "minecraft:post/blit", file
        assert second["inputs"] == [{"sampler_name": "In", "target": "swap"}], file
        assert second["output"] == "minecraft:main", file

print("PASS: ImpactFX phase 2 shader files and 10 post-effect definitions")
