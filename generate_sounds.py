#!/usr/bin/env python3
"""
Generate placeholder sound files for BonePipe mod
Requires: pip install numpy scipy
"""

import os
import struct
import math

def generate_sine_wave(frequency, duration, sample_rate=22050, amplitude=0.3):
    """Generate a simple sine wave"""
    num_samples = int(duration * sample_rate)
    samples = []
    
    for i in range(num_samples):
        t = i / sample_rate
        # Apply envelope (fade in/out)
        envelope = 1.0
        fade_samples = sample_rate // 20  # 50ms fade
        if i < fade_samples:
            envelope = i / fade_samples
        elif i > num_samples - fade_samples:
            envelope = (num_samples - i) / fade_samples
        
        value = amplitude * envelope * math.sin(2 * math.pi * frequency * t)
        samples.append(value)
    
    return samples

def write_wav(filename, samples, sample_rate=22050):
    """Write WAV file (16-bit mono)"""
    num_samples = len(samples)
    bytes_per_sample = 2
    num_channels = 1
    byte_rate = sample_rate * num_channels * bytes_per_sample
    block_align = num_channels * bytes_per_sample
    
    with open(filename, 'wb') as f:
        # RIFF header
        f.write(b'RIFF')
        f.write(struct.pack('<I', 36 + num_samples * bytes_per_sample))
        f.write(b'WAVE')
        
        # fmt chunk
        f.write(b'fmt ')
        f.write(struct.pack('<I', 16))  # Chunk size
        f.write(struct.pack('<H', 1))   # Audio format (PCM)
        f.write(struct.pack('<H', num_channels))
        f.write(struct.pack('<I', sample_rate))
        f.write(struct.pack('<I', byte_rate))
        f.write(struct.pack('<H', block_align))
        f.write(struct.pack('<H', 16))  # Bits per sample
        
        # data chunk
        f.write(b'data')
        f.write(struct.pack('<I', num_samples * bytes_per_sample))
        
        # Write samples
        for sample in samples:
            # Convert to 16-bit signed integer
            value = int(sample * 32767)
            value = max(-32768, min(32767, value))
            f.write(struct.pack('<h', value))

def main():
    print("Generating BonePipe sound files...")
    
    # Create directory
    sounds_dir = "src/main/resources/assets/bonepipe/sounds"
    os.makedirs(sounds_dir, exist_ok=True)
    
    # Generate transfer sound (whoosh)
    print("  Generating transfer.ogg...")
    transfer_samples = []
    for freq in [400, 600, 800]:  # Ascending tones
        transfer_samples.extend(generate_sine_wave(freq, 0.1, amplitude=0.2))
    write_wav(f"{sounds_dir}/transfer.wav", transfer_samples)
    
    # Generate connect sound (beep up)
    print("  Generating connect.ogg...")
    connect_samples = generate_sine_wave(800, 0.15, amplitude=0.25)
    write_wav(f"{sounds_dir}/connect.wav", connect_samples)
    
    # Generate disconnect sound (beep down)
    print("  Generating disconnect.ogg...")
    disconnect_samples = generate_sine_wave(400, 0.15, amplitude=0.25)
    write_wav(f"{sounds_dir}/disconnect.wav", disconnect_samples)
    
    print("✓ Sound files generated!")
    print(f"  Files created in: {sounds_dir}")
    print("\nNote: WAV files created. For production, convert to OGG:")
    print("  ffmpeg -i transfer.wav -c:a libvorbis -q:a 4 transfer.ogg")

if __name__ == "__main__":
    main()
