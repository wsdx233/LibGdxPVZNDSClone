#!/usr/bin/env python3
"""
Test the rounding formula issue
"""

def derive_key_from_kill_count(kill_count: int) -> int:
    """Original formula"""
    return (kill_count * 7 + 13) % 256

def apply_rounding(kill_count: int) -> int:
    """The rounding formula that was added - using integer division like Kotlin"""
    # In Kotlin: ((zombieKillCount + 5) / 10) * 10 - 1
    # Kotlin's / operator on Int types performs integer division (truncates)
    return ((kill_count + 5) // 10) * 10 - 1

def xor_decrypt(data: bytes, key: int) -> bytes:
    """XOR decryption"""
    return bytes([b ^ key for b in data])

# The encrypted flag
encrypted = bytes([
    0x4f, 0x45, 0x48, 0x4e, 0x52, 0x50, 0x19, 0x5c,
    0x76, 0x42, 0x18, 0x45, 0x45, 0x1a, 0x4d, 0x76,
    0x5d, 0x41, 0x1a, 0x44, 0x76, 0x1d, 0x45, 0x45,
    0x54
])

correct_flag = "flag{y0u_k1ll3d_th3m_4ll}"

print("=" * 70)
print("Testing Rounding Formula Issue")
print("=" * 70)

print("\n1. Testing what the rounding formula produces:")
print("-" * 70)
test_values = [9, 19, 29, 39, 49, 59, 69, 79, 89, 99,
               109, 119, 129, 139, 149, 159, 169, 179, 189, 199,
               209, 219, 229, 239, 249, 259, 260, 261, 269, 279, 289, 299]

for val in test_values:
    rounded = apply_rounding(val)
    print(f"  killCount={val:3d} -> rounded={rounded:3d}")

print("\n2. Testing which values decrypt successfully WITH ROUNDING:")
print("-" * 70)

successful_counts = []
for count in range(0, 300):
    rounded = apply_rounding(count)
    key = derive_key_from_kill_count(rounded)
    decrypted = xor_decrypt(encrypted, key)
    try:
        result = decrypted.decode('utf-8')
        if result == correct_flag:
            successful_counts.append(count)
            print(f"  ✓ killCount={count:3d} -> rounded={rounded:3d} -> key={key:3d} (0x{key:02x}) -> SUCCESS")
    except:
        pass

print("\n2b. Testing which ROUNDED values (n*10-1) decrypt successfully:")
print("-" * 70)

for n in range(0, 30):
    rounded_value = n * 10 - 1
    if rounded_value < 0:
        continue
    key = derive_key_from_kill_count(rounded_value)
    decrypted = xor_decrypt(encrypted, key)
    try:
        result = decrypted.decode('utf-8')
        status = "✓ SUCCESS" if result == correct_flag else "✗ FAIL"
        print(f"  n={n:2d} -> value={rounded_value:3d} -> key={key:3d} (0x{key:02x}) -> {status}: {result[:30]}")
    except Exception as e:
        print(f"  n={n:2d} -> value={rounded_value:3d} -> key={key:3d} (0x{key:02x}) -> ✗ FAIL: [decode error]")

print(f"\n3. Summary:")
print("-" * 70)
print(f"Total successful kill counts: {len(successful_counts)}")
print(f"Successful values: {successful_counts[:20]}{'...' if len(successful_counts) > 20 else ''}")

print("\n4. The problem:")
print("-" * 70)
print("The formula ((killCount + 5) / 10) * 10 - 1 always produces values ending in 9")
print("For example:")
print("  - killCount 255-264 all round to 259")
print("  - killCount 265-274 all round to 269")
print("  - killCount 245-254 all round to 249")
print("\nThis means ANY value in the range [255, 264] will work!")

print("\n5. Solution:")
print("-" * 70)
print("We need to check the EXACT kill count, not a rounded value.")
print("The encryption was designed for exactly 260 kills.")
print("\nRecommended fix: Remove the rounding formula entirely")
print("Change line 160 from:")
print("  val killCountDecrypted = decryptWithKillCount(killCountEncryptedFlag, ((zombieKillCount + 5) / 10) * 10 - 1)")
print("To:")
print("  val killCountDecrypted = decryptWithKillCount(killCountEncryptedFlag, zombieKillCount)")

print("\n6. Why you saw 259 work initially:")
print("-" * 70)
print("If the actual kill count was 259 when you tested, it means:")
print("  - Either the zombie counting has an off-by-one error")
print("  - Or preview zombies are being counted when they shouldn't be")
print("  - Or zombies are being counted multiple times")
print("\nLet's verify the correct key for 259 vs 260:")
key_259 = derive_key_from_kill_count(259)
key_260 = derive_key_from_kill_count(260)
print(f"  Key for 259: {key_259} (0x{key_259:02x})")
print(f"  Key for 260: {key_260} (0x{key_260:02x})")

# Test both
decrypted_259 = xor_decrypt(encrypted, key_259)
decrypted_260 = xor_decrypt(encrypted, key_260)
print(f"\n  Decrypt with 259: {decrypted_259.decode('utf-8', errors='replace')}")
print(f"  Decrypt with 260: {decrypted_260.decode('utf-8', errors='replace')}")
