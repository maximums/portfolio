@file:Suppress("unused")

package com.cdodi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import blog.composeapp.generated.resources.Res
import com.cdodi.components.RuntimeShader

private const val FALLBACK_SHADER = """
    uniform int width;
    uniform int height;
    uniform float iTime;

    half4 main(vec2 fragCoord) {
        return half4(0.05, 0.05, 0.15, 1.0);
    }
"""

// TODO need to test it first
const val PIXEL_MELT_SHADER = """
    uniform float2 resolution;
    uniform float progress;
    uniform float2 direction; // e.g., (1.0, 0.2) blows right and slightly down
    uniform shader composable;

    // Pseudo-random noise generator
    float random(float2 st) {
        return fract(sin(dot(st.xy, float2(12.9898,78.233))) * 43758.5453123);
    }

    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / resolution;
        
        // 1. Smaller grid for finer "dust" particles instead of chunky blocks
        float gridSize = max(40.0, 800.0 * (1.0 - progress));
        float2 pixelatedUv = floor(uv * gridSize) / gridSize;
        
        // 2. Core noise for the particle
        float noise = random(pixelatedUv);
        
        // 3. Add turbulence (scatter) so the wind feels chaotic
        float2 scatter = float2(
            random(pixelatedUv + 1.0) - 0.5, 
            random(pixelatedUv + 2.0) - 0.5
        ) * 0.4; // 0.4 is the wind turbulence strength
        
        // 4. Calculate final movement vector
        // Multiplies the base direction + turbulence by the progress
        float2 movement = (direction + scatter) * (progress * noise * 1.5);
        
        // 5. Offset the coordinate
        float2 offsetCoord = fragCoord - (movement * resolution);
        
        // 6. Sample the original UI
        half4 color = composable.eval(offsetCoord);
        
        // 7. Dissolve threshold: particles vanish as they blow away
        float alpha = step(progress, noise);
        
        return color * alpha;
    }
"""

@Composable
fun rememberShader(name: String): State<RuntimeShader> = produceState(RuntimeShader(FALLBACK_SHADER)) {
    value = RuntimeShader(Res.readBytes("files/$name.sksl").decodeToString())
}
