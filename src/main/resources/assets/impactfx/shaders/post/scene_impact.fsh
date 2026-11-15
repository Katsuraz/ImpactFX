#version 330
#extension GL_ARB_separate_shader_objects : require

// ImpactFX 0.3: world-aware color transformation with short screen-space shock pulses.
uniform sampler2D InSampler;
layout(location = 0) in vec2 texCoord;
layout(location = 0) out vec4 fragColor;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform ImpactFXConfig {
    float Strength;
    float Style;
    float WaveRadius; // normalized around screen center
    float Distortion; // 0 for disabled, always <= 0.023 for the default power preset
    float Punch;      // transient scene-scale impulse; never changes the real camera
    vec2 Origin;      // world-projected impact point, quantized to a screen grid
};

float luminance(vec3 color) {
    return dot(color, vec3(0.2126, 0.7152, 0.0722));
}

float outline(vec2 uv) {
    vec2 duv = 1.35 / max(InSize, vec2(1.0));
    float le = luminance(texture(InSampler, clamp(uv - vec2(duv.x, 0.0), 0.001, 0.999)).rgb);
    float ri = luminance(texture(InSampler, clamp(uv + vec2(duv.x, 0.0), 0.001, 0.999)).rgb);
    float up = luminance(texture(InSampler, clamp(uv + vec2(0.0, duv.y), 0.001, 0.999)).rgb);
    float dn = luminance(texture(InSampler, clamp(uv - vec2(0.0, duv.y), 0.001, 0.999)).rgb);
    return clamp((abs(ri - le) + abs(up - dn)) * 2.8, 0.0, 1.0);
}

vec3 stylize(vec2 uv, vec3 original) {
    float l = luminance(original);
    if (Style < 0.5) { // Manga: harder ink silhouettes, larger pale surfaces.
        float blackAndWhite = smoothstep(0.405, 0.575, l);
        float line = outline(uv);
        return vec3(clamp(0.015 + blackAndWhite * 0.98 - line * 0.79, 0.0, 1.0));
    }
    if (Style < 1.5) { // Anime: cel-shading with complementary ink linework.
        float line = outline(uv);
        vec3 saturated = mix(vec3(l), original, 1.85);
        vec3 cel = floor(clamp(saturated, 0.0, 1.0) * 5.0 + 0.5) / 5.0;
        return clamp(cel * (1.09 - line * 0.66), 0.0, 1.0);
    }
    if (Style < 2.5) { // Ink brush: parchment shading and sharp black strokes.
        float line = outline(uv);
        float grain = fract(sin(dot(floor(uv * InSize / 3.0), vec2(12.9898, 78.233))) * 43758.5453);
        float brush = smoothstep(0.375, 0.64, l + (grain - 0.5) * 0.16);
        return vec3(0.97, 0.945, 0.86) * clamp(brush * (1.0 - line * 0.85), 0.02, 1.0);
    }
    if (Style < 3.5) { // Retro: stepped pixels / punchy small arcade palette.
        vec2 block = vec2(6.0) / max(InSize, vec2(1.0));
        vec2 pixelUV = (floor(uv / block) + 0.5) * block;
        vec3 sampled = texture(InSampler, clamp(pixelUV, 0.0, 1.0)).rgb;
        return floor(clamp(sampled * 1.18, 0.0, 1.0) * 4.0 + 0.5) / 4.0;
    }
    if (Style < 4.5) { // Cinematic: compressed lights, dark vignette.
        vec3 muted = mix(original, vec3(l), 0.74);
        float vignette = 1.0 - smoothstep(0.23, 0.85,
            length((uv - vec2(0.5)) * vec2(1.15, 1.0))) * 0.41;
        return clamp(((muted - 0.5) * 1.28 + 0.5) * vignette, 0.0, 1.0);
    }
    // Overdrive: near-binary silhouettes with ink weight and restrained violet edge fire.
    float edge = outline(uv);
    float hard = smoothstep(0.43, 0.535, l);
    vec3 monochrome = vec3(clamp(0.016 + hard * 0.975 - edge * 0.90, 0.0, 1.0));
    float accent = edge * (1.0 - abs(2.0 * hard - 1.0)) * 0.66;
    return clamp(monochrome + vec3(0.36, 0.12, 0.70) * accent, 0.0, 1.0);
}

void main() {
    vec2 origin = clamp(Origin,vec2(0.01),vec2(0.99));
    vec2 fromCenter = texCoord - origin;
    float aspect = max(0.5, min(3.0, InSize.x / max(InSize.y, 1.0)));
    vec2 corrected = vec2(fromCenter.x * aspect, fromCenter.y);
    float r = max(length(corrected), 0.00001);
    // Distortion and punch are centered on the projected hit position.
    float band = exp(-pow((r - WaveRadius) / 0.038, 2.0));
    vec2 direction = normalize(corrected + vec2(0.000001));
    vec2 uv = origin + fromCenter / (1.0 + clamp(Punch, 0.0, 0.08));
    uv += vec2(direction.x / aspect, direction.y) * band * clamp(Distortion, 0.0, 0.028);
    uv = clamp(uv, vec2(0.001), vec2(0.999));

    vec4 frame = texture(InSampler, uv);
    vec3 stylized = stylize(uv, frame.rgb);
    fragColor = vec4(mix(frame.rgb, stylized, clamp(Strength, 0.0, 1.0)), frame.a);
}
