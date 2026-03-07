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
    uniform shader composable; // The actual Compose UI

    // Pseudo-random noise generator based on coordinates
    float random(float2 st) {
        return fract(sin(dot(st.xy, float2(12.9898,78.233))) * 43758.5453123);
    }

    half4 main(float2 fragCoord) {
        // 1. Normalize coordinates (0.0 to 1.0)
        float2 uv = fragCoord / resolution;
        
        // 2. Grid size gets chunkier as progress increases
        float gridSize = max(10.0, 500.0 * (1.0 - progress));
        
        // 3. Snap coordinates to the grid to create "pixels"
        float2 pixelatedUv = floor(uv * gridSize) / gridSize;
        
        // 4. Generate random value for each pixel block
        float noise = random(pixelatedUv);
        
        // 5. Melt downwards: offset Y based on progress and noise
        float2 offset = float2(0.0, progress * noise * 0.5);
        float2 sampledCoord = fragCoord - (offset * resolution);
        
        // 6. Sample the original UI at the new melted coordinate
        half4 color = composable.eval(sampledCoord);
        
        // 7. Dissolve alpha: pixels disappear randomly as progress increases
        float alpha = step(progress, noise); 
        
        return color * alpha;
    }
"""

@Composable
fun rememberShader(name: String): State<RuntimeShader> = produceState(RuntimeShader(FALLBACK_SHADER)) {
    value = RuntimeShader(Res.readBytes("files/$name.sksl").decodeToString())
}
