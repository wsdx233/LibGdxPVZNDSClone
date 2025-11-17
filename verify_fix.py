#!/usr/bin/env python3
"""
Verify that the encryption fix works correctly.
Only kill counts 255-264 (rounding to 260) should decrypt successfully.
"""

def round_to_nearest_10(kill_count):
    """Round kill count to nearest 10"""
    return ((kill_count + 5) // 10) * 10

def derive_key_from_kill_count(kill_count):
    """Derive encryption key from kill count - matches Kotlin logic"""
    rounded = round_to_nearest_10(kill_count)

    # Only use 260 if rounded equals 260
    if rounded == 260:
        effective_count = 260
    else:
        effective_count = kill_count * 3 + 17

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

print("=" * 80)
print("验证击杀数加密修复")
print("=" * 80)
print("\n预期行为:")
print("  ✓ 击杀数 255-264 (四舍五入到 260): 应该解密出正确的 flag")
print("  ✗ 其他所有击杀数: 应该产生乱码\n")

# Test various kill counts
test_cases = [
    (1, False, "Main.kt 中的测试值"),
    (10, False, "10的倍数"),
    (245, False, "太低 - 四舍五入到 250"),
    (250, False, "四舍五入到 250"),
    (254, False, "刚好低于范围 - 四舍五入到 250"),
    (255, True, "下界 - 四舍五入到 260 ✓"),
    (258, True, "范围中间 - 四舍五入到 260 ✓"),
    (260, True, "精确目标 - 四舍五入到 260 ✓"),
    (262, True, "范围中间 - 四舍五入到 260 ✓"),
    (264, True, "上界 - 四舍五入到 260 ✓"),
    (265, False, "刚好高于范围 - 四舍五入到 270"),
    (270, False, "四舍五入到 270"),
    (259, False, "旧漏洞值 10*26-1"),
    (269, False, "旧漏洞值 10*27-1"),
    (279, False, "旧漏洞值 10*28-1"),
]

print(f"{'击杀数':<8} {'四舍五入':<10} {'密钥':<8} {'解密结果':<30} {'状态':<8} {'说明'}")
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
        result_str = "<乱码>"

    # Check if result matches expectation
    if is_valid == should_work:
        status = "通过"
        success_count += 1
        symbol = "✓" if should_work else "✗"
    else:
        status = "失败"
        fail_count += 1
        symbol = "✗"

    print(f"{count:<8} {rounded:<10} 0x{key:02x}     {result_str:<30} {symbol} {status:<6} {note}")

print("-" * 100)
print(f"\n测试结果: {success_count} 通过, {fail_count} 失败")

if fail_count == 0:
    print("\n✓ 成功! 所有测试通过。只有击杀数 255-264 可以解密 flag。")
    print("  旧的漏洞 (10*n-1) 已修复！")
else:
    print(f"\n✗ 失败! {fail_count} 个测试失败。请检查加密逻辑。")
