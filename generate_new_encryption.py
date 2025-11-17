#!/usr/bin/env python3
"""
Generate encrypted flag using the new complex encryption scheme.
Only kill counts that round to 260 (255-264) will decrypt correctly.
"""

def derive_key_from_kill_count(kill_count):
    """Derive encryption key - matches Kotlin logic exactly"""
    # Layer 1: Apply rounding transformation
    rounded = ((kill_count + 5) // 10) * 10

    # Layer 2: Hash-like transformation using prime numbers
    hash1 = (rounded * 31 + 17) % 997
    hash2 = (rounded * 37 + 23) % 991
    hash3 = (rounded * 41 + 29) % 983

    # Layer 3: Combine hashes with XOR and rotation
    combined = (hash1 ^ (hash2 << 3) ^ (hash3 >> 2)) % 256

    # Layer 4: Generate multi-byte key using seed
    seed = (rounded * 7 + hash1 + hash2 + hash3) % 65536
    key = []
    state = seed
    for i in range(16):
        state = (state * 1103515245 + 12345) & 0x7fffffff
        key.append(((state >> 16) % 256) & 0xFF)

    return bytes(key)

def encrypt_with_kill_count(data, kill_count):
    """Encrypt data using kill count - matches Kotlin decryption logic"""
    key_stream = derive_key_from_kill_count(kill_count)
    result = bytearray(len(data))

    for i in range(len(data)):
        # Use different key bytes for different positions
        key_byte = key_stream[i % len(key_stream)]
        # Add position-dependent transformation
        position_key = ((i * 13 + 7) % 256) & 0xFF
        result[i] = (data[i] ^ key_byte ^ position_key) & 0xFF

    return bytes(result)

# The flag we want to encrypt
flag = b"flag{y0u_k1ll3d_260_z0mb}"

print("=" * 70)
print("生成新的加密 Flag")
print("=" * 70)

# Encrypt with kill count 260
encrypted = encrypt_with_kill_count(flag, 260)

# Print as Kotlin byte array
print("\n将以下代码复制到 FlagScreen.kt:")
print("-" * 70)
print("private val killCountEncryptedFlag = byteArrayOf(")
for i in range(0, len(encrypted), 4):
    chunk = encrypted[i:i+4]
    hex_values = ", ".join([f"0x{b:02x}.toByte()" for b in chunk])
    if i + 4 < len(encrypted):
        print(f"    {hex_values},")
    else:
        print(f"    {hex_values}")
print(")")
print("-" * 70)

# Verify decryption works
print("\n验证解密:")
decrypted = encrypt_with_kill_count(encrypted, 260)
print(f"使用 killCount=260 解密: {decrypted.decode('utf-8')}")

# Test with wrong values
print("\n测试错误的击杀数:")
test_values = [1, 10, 250, 255, 258, 260, 262, 264, 270]
for count in test_values:
    rounded = ((count + 5) // 10) * 10
    decrypted = encrypt_with_kill_count(encrypted, count)
    try:
        result = decrypted.decode('utf-8')
        is_valid = result.startswith('flag{') and result.endswith('}')
        status = "✓" if is_valid else "✗"
        print(f"  killCount={count:3d} (rounds to {rounded:3d}): {status} {result if is_valid else '<乱码>'}")
    except:
        print(f"  killCount={count:3d} (rounds to {rounded:3d}): ✗ <解码失败>")

print("\n" + "=" * 70)
