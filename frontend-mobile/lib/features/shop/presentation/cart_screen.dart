import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'shop_screen.dart';

class CartScreen extends ConsumerStatefulWidget {
  const CartScreen({super.key});

  @override
  ConsumerState<CartScreen> createState() => _CartScreenState();
}

class _CartScreenState extends ConsumerState<CartScreen> {
  final _addressCtrl = TextEditingController();
  bool _placing = false;

  @override
  void dispose() { _addressCtrl.dispose(); super.dispose(); }

  Future<void> _placeOrder() async {
    final cart = ref.read(cartProvider);
    if (cart.isEmpty) return;
    if (_addressCtrl.text.trim().isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please enter a shipping address')));
      return;
    }

    setState(() => _placing = true);
    try {
      final items = cart.map((i) => {'productId': i['productId'], 'quantity': i['quantity']}).toList();
      await ref.read(shopRepoProvider).placeOrder(
          List<Map<String, dynamic>>.from(items), _addressCtrl.text.trim());
      ref.read(cartProvider.notifier).clear();
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Order placed successfully! 🎉')));
        context.pop();
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('Order failed: $e')));
      }
    } finally {
      if (mounted) setState(() => _placing = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final cart = ref.watch(cartProvider);
    final total = ref.watch(cartProvider.notifier).total;

    return Scaffold(
      backgroundColor: const Color(0xFF0F0F0F),
      appBar: AppBar(
        backgroundColor: const Color(0xFF0F0F0F),
        foregroundColor: Colors.white,
        title: const Text('My Cart', style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600)),
        elevation: 0,
      ),
      body: cart.isEmpty
          ? const Center(child: Text('Your cart is empty', style: TextStyle(color: Colors.white38)))
          : Column(children: [
              Expanded(
                child: ListView(
                  padding: const EdgeInsets.all(16),
                  children: [
                    ...cart.map((item) => Container(
                      margin: const EdgeInsets.only(bottom: 10),
                      padding: const EdgeInsets.all(14),
                      decoration: BoxDecoration(color: const Color(0xFF1E1E1E), borderRadius: BorderRadius.circular(12)),
                      child: Row(children: [
                        Expanded(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                          Text(item['productName'] as String, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w600)),
                          Text('\$${item['unitPrice']}', style: const TextStyle(color: Colors.white54, fontSize: 12)),
                        ])),
                        Row(children: [
                          _QtyBtn(icon: Icons.remove, onTap: () => ref.read(cartProvider.notifier).updateQuantity(item['productId'] as int, (item['quantity'] as int) - 1)),
                          Padding(
                            padding: const EdgeInsets.symmetric(horizontal: 10),
                            child: Text('${item['quantity']}', style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w700)),
                          ),
                          _QtyBtn(icon: Icons.add, onTap: () => ref.read(cartProvider.notifier).updateQuantity(item['productId'] as int, (item['quantity'] as int) + 1)),
                        ]),
                      ]),
                    )),
                    const SizedBox(height: 16),
                    TextField(
                      controller: _addressCtrl,
                      style: const TextStyle(color: Colors.white, fontSize: 14),
                      decoration: InputDecoration(
                        labelText: 'Shipping Address',
                        labelStyle: const TextStyle(color: Colors.white38),
                        filled: true, fillColor: const Color(0xFF1E1E1E),
                        border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide.none),
                      ),
                    ),
                  ],
                ),
              ),
              Container(
                padding: const EdgeInsets.all(20),
                decoration: const BoxDecoration(color: Color(0xFF1A1A1A), border: Border(top: BorderSide(color: Color(0xFF2A2A2A)))),
                child: Column(children: [
                  Row(children: [
                    const Text('Total', style: TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.w600)),
                    const Spacer(),
                    Text('\$${total.toStringAsFixed(2)}', style: const TextStyle(color: Colors.white, fontSize: 20, fontWeight: FontWeight.w800)),
                  ]),
                  const SizedBox(height: 14),
                  SizedBox(
                    width: double.infinity, height: 50,
                    child: ElevatedButton(
                      onPressed: _placing ? null : _placeOrder,
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.white, foregroundColor: Colors.black,
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                      ),
                      child: _placing
                          ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2))
                          : const Text('Place Order', style: TextStyle(fontWeight: FontWeight.w700)),
                    ),
                  ),
                ]),
              ),
            ]),
    );
  }
}

class _QtyBtn extends StatelessWidget {
  final IconData icon;
  final VoidCallback onTap;
  const _QtyBtn({required this.icon, required this.onTap});

  @override
  Widget build(BuildContext context) => GestureDetector(
        onTap: onTap,
        child: Container(
          width: 28, height: 28,
          decoration: BoxDecoration(color: const Color(0xFF2A2A2A), borderRadius: BorderRadius.circular(8)),
          child: Icon(icon, color: Colors.white, size: 16),
        ),
      );
}
