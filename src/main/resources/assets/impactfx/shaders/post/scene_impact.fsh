#version 330
#extension GL_ARB_separate_shader_objects : require

// 26.3 built-in post-effect shader. Runs on the world framebuffer before the HUD.
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
};

float luminance(vec3 c) {
    return dot(c, vec3(0.2126, 0.7152, 0.0722));
}

float outline(vec2 uv) {
    vec2 stepUV = 1.4 / max(InSize, vec2(1.0));
    float leftValue  = luminance(texture(InSampler, uv - vec2(stepUV.x, 0.0)).rgb);
    float rightValue = luminance(texture(InSampler, uv + vec2(stepUV.x, 0.0)).rgb);
    float upValue    = luminance(texture(InSampler, uv + vec2(0.0, stepUV.y)).rgb);
    float downValue  = luminance(texture(InSampler, uv - vec2(0.0, stepUV.y)).rgb);
    return clamp((abs(rightValue - leftValue) + abs(upValue - downValue)) * 2.4, 0.0, 1.0);
}

vec3 stylize(vec2 uv, vec3 source) {
    float luma = luminance(source);
    if (Style < 0.5) {  // MANGA: thresholded black/white with scene-edge ink.
        float stepValue = smoothstep(0.31, 0.68, luma);
        float contour = outline(uv);
        return vec3(clamp(0.04 + stepValue * 0.93 - contour * 0.58, 0.0, 1.0));
    }
    if (Style < 1.5) { // ANIME: saturated cel shading with contrasting outlines.
        float contour = outline(uv);
        vec3 saturation = mix(vec3(luma), source, 1.75);
        vec3 cel = floor(clamp(saturation, 0.0, 1.0) * 5.0 + 0.5) / 5.0;
        return clamp(cel * (1.06 - 0.52 * contour), 0.0, 1.0);
    }
    if (Style < 2.5) { // INK: warm paper, deep ink contours, stable print texture.
        float contour = outline(uv);
        float dotPattern = fract(sin(dot(floor(uv * InSize / 3.0), vec2(12.9898, 78.233))) * 43758.5453);
        float ink = smoothstep(0.36, 0.72, luma + (dotPattern - 0.5) * 0.18);
        ink *= 1.0 - contour * 0.8;
        return vec3(0.96, 0.93, 0.84) * clamp(ink, 0.02, 1.0);
    }
    if (Style < 3.5) { // RETRO: intentional pixel blocks, quantized 12-shade look.
        vec2 tile = vec2(6.0, 6.0) / max(InSize, vec2(1.0));
        vec2 uvPixel = (floor(uv / tile) + 0.5) * tile;
        vec3 sampled = texture(InSampler, uvPixel).rgb;
        return floor(clamp(sampled * 1.18, 0.0, 1.0) * 4.0 + 0.5) / 4.0;
    }
    if (Style < 4.5) { // CINEMATIC: crushed shadows, soft desaturation and vignette.
        vec3 desaturated = mix(source, vec3(luma), 0.73);
        float vignette = 1.0 - smoothstep(0.23, 0.82, length((uv - vec2(0.5)) * vec2(1.15, 1.0))) * 0.35;
        return clamp(((desaturated - 0.5) * 1.22 + 0.5) * vignette, 0.0, 1.0);
    }
    return source;
}

void main() {
    vec4 scene = texture(InSampler, texCoord);
    float amount = clamp(Strength, 0.0, 1.0);
    vec3 stylized = stylize(texCoord, scene.rgb);
    fragColor = vec4(mix(scene.rgb, stylized, amount), scene.a);
}
