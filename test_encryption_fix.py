#!/usr/bin/env python3
"""
Test that the encryption fix works correctly.
Only kill counts 255-264 (rounding to 260) should decrypt successfully.
"""

def round_to_nearest_10(kill_count):
    """Round kill count to nearest 10"""
    return ((kill_count + 5) // 10) * 10

def derive_key_from_kill_count(kill_count):
    """Derive encryption key from kill count - matches Kotlin logic"""
    rounded = round_to_nearest_10(kill_count)

    # Only use correct value if rounded equals 260
    if rounded == 260:
        effective_count = 260
    else:
        effective_count = rounded * 3 + 17

    # Formula: (effectiveCount * 7 + 13) % 256
    return ((effective_count * 7 + 13) % 256) & 0xFF

def xor_decrypt(data, key):
    """XOR decrypt data with key"""
    return bytes([b ^ key for b in data])

# The encrypted flag from FlagScreen.kt
encrypted_flag = bytes([
    0x4f, 0x45, 0x48, 0x4e,
    0x52, 0x50, 0x19, 0x5c,
    0x76, 0x42, 0x18, 0x45,
    0x45, 0x1a, 0x4d, 0x76,
    0x1b, 0x1f, 0x19, 0x76,
    0x53, 0x19, 0x44, 0x4b,
    0x54
])

print("=" * 70)
print("Testing Kill Count Encryption Fix")
print("=" * 70)
print("\nExpected behavior:")
print("  - Kill counts 255-264 (round to 260): Should decrypt to valid flag")
print("  - All other kill counts: Should produce garbage\n")

# Test various kill counts
test_cases = [
    (245, False, "Too low - rounds to 250"),
    (250, False, "Rounds to 250"),
    (254, False, "Just below range - rounds to 250"),
    (255, True, "Lower bound - rounds to 260"),
    (258, True, "Middle of range - rounds to 260"),
    (260, True, "Exact target - rounds to 260"),
    (262, True, "Middle of range - rounds to 260"),
    (264, True, "Upper bound - rounds to 260"),
    (265, False, "Just above range - rounds to 270"),
    (270, False, "Rounds to 270"),
    (259, False, "Old vulnerable value - rounds to 260 but should work now"),
    (269, False, "Old vulnerable value - rounds to 270"),
]

print(f"{'Count':<6} {'Rounds to':<10} {'Key':<6} {'Result':<30} {'Status':<10} {'Note'}")
print("-" * 100)

success_count = 0
fail_count = 0

for count, should_work, note in test_cases:
    rounded = round_to_nearest_10(count)
    key = derive_key_from_kill_count(count)
    decrypted = xor_decrypt(encrypted_flag, key)

    try:
        result = decrypted.decode('utf-8')
        is_valid = result.startswith('flag{') and result.endswith('}')
        result_str = result if is_valid else result[:20] + "..."
    except:
        is_valid = False
        result_str = "<binary garbage>"

    # Check if result matches expectation
    if is_valid == should_work:
        status = "PASS"
        success_count += 1
    else:
        status = "FAIL"
        fail_count += 1

    print(f"{count:<6} {rounded:<10} 0x{key:02x}   {result_str:<30} {status:<10} {note}")

print("-" * 100)
print(f"\nTest Results: {success_count} passed, {fail_count} failed")

if fail_count == 0:
    print("\n SUCCESS! All tests passed. Only kill counts 255-264 can decrypt the flag.")
else:
    print(f"\n FAILURE! {fail_count} test(s) failed. Please review the encryption logic.")
