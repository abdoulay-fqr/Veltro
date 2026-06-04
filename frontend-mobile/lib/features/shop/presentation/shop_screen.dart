import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../data/shop_repository.dart';

final shopRepoProvider = Provider<ShopRepository>((ref) => ShopRepository());

final cartProvider = StateNotifierProvider<CartNotifier, List<Map<String, dynamic>>>(
  (ref) => CartNotifier(),
);

class CartNotifier extends StateNotifier<List<Map<String, dynamic>>> {
  CartNotifier() : super([]);

  void addItem(Map<String, dynamic> product, int quantity) {
    final existing = state.indexWhere((i) => i['productId'] == product['id']);
    if (existing >= 0) {
      final updated = List<Map<String, dynamic>>.from(state);
      updated[existing] = {...updated[existing], 'quantity': (updated[existing]['quantity'] as int) + quantity};
      state = updated;
    } else {
      state = [...state, {'productId': product['id'], 'productName': product['name'], 'unitPrice': product['price'], 'quantity': quantity}];
    }
  }

  void removeItem(int productId) {
    state = state.where((i) => i['productId'] != productId).toList();
  }

  void updateQuantity(int productId, int qty) {
    if (qty <= 0) { removeItem(productId); return; }
    state = state.map((i) => i['productId'] == productId ? {...i, 'quantity': qty} : i).toList();
  }

  void clear() => state = [];
  double get total => state.fold(0.0, (s, i) => s + (i['unitPrice'] as double) * (i['quantity'] as int));
}

final productsProvider = FutureProvider.family<List<dynamic>, String?>((ref, category) async {
  return ref.read(shopRepoProvider).getProducts(category: category);
});

class ShopScreen extends ConsumerStatefulWidget {
  const ShopScreen({super.key});

  @override
  ConsumerState<ShopScreen> createState() => _ShopScreenState();
}

class _ShopScreenState extends ConsumerState<ShopScreen> {
  String? _selectedCategory;
  final _categories = [null, 'SUPPLEMENT', 'CLOTHING', 'EQUIPMENT'];
  final _categoryLabels = ['All', 'Supplements', 'Clothing', 'Equipment'];

  @override
  Widget build(BuildContext context) {
    final productsAsync = ref.watch(productsProvider(_selectedCategory));
    final cartCount = ref.watch(cartProvider).length;

    return Scaffold(
      backgroundColor: const Color(0xFF0F0F0F),
      appBar: AppBar(
        backgroundColor: const Color(0xFF0F0F0F),
        foregroundColor: Colors.white,
        title: const Text('Shop', style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600)),
        elevation: 0,
        actions: [
          Stack(
            children: [
              IconButton(
                icon: const Icon(Icons.shopping_cart_outlined, size: 22),
                onPressed: () => context.push('/cart'),
              ),
              if (cartCount > 0)
                Positioned(
                  right: 8, top: 8,
                  child: Container(
                    width: 16, height: 16,
                    decoration: const BoxDecoration(color: Colors.red, shape: BoxShape.circle),
                    child: Center(child: Text('$cartCount',
                        style: const TextStyle(color: Colors.white, fontSize: 10, fontWeight: FontWeight.w700))),
                  ),
                ),
            ],
          ),
        ],
      ),
      body: Column(children: [
        // Category chips
        SizedBox(
          height: 48,
          child: ListView.builder(
            scrollDirection: Axis.horizontal,
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            itemCount: _categories.length,
            itemBuilder: (_, i) => Padding(
              padding: const EdgeInsets.only(right: 8),
              child: ChoiceChip(
                label: Text(_categoryLabels[i]),
                selected: _selectedCategory == _categories[i],
                onSelected: (_) => setState(() => _selectedCategory = _categories[i]),
                backgroundColor: const Color(0xFF1E1E1E),
                selectedColor: Colors.white,
                labelStyle: TextStyle(
                  color: _selectedCategory == _categories[i] ? Colors.black : Colors.white70,
                  fontSize: 13,
                ),
              ),
            ),
          ),
        ),

        // Product grid
        Expanded(
          child: productsAsync.when(
            loading: () => const Center(child: CircularProgressIndicator()),
            error: (e, _) => Center(child: Text('Error: $e', style: const TextStyle(color: Colors.white54))),
            data: (products) => GridView.builder(
              padding: const EdgeInsets.all(16),
              gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: 2, crossAxisSpacing: 12, mainAxisSpacing: 12, childAspectRatio: 0.75,
              ),
              itemCount: products.length,
              itemBuilder: (_, i) {
                final p = products[i] as Map<String, dynamic>;
                return _ProductCard(
                  product: p,
                  onAddToCart: () {
                    ref.read(cartProvider.notifier).addItem(p, 1);
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(content: Text('${p['name']} added to cart'), duration: const Duration(seconds: 1)),
                    );
                  },
                );
              },
            ),
          ),
        ),
      ]),
    );
  }
}

class _ProductCard extends StatelessWidget {
  final Map<String, dynamic> product;
  final VoidCallback onAddToCart;
  const _ProductCard({required this.product, required this.onAddToCart});

  @override
  Widget build(BuildContext context) {
    final stock = product['stockQuantity'] as int? ?? 0;
    return Container(
      decoration: BoxDecoration(color: const Color(0xFF1E1E1E), borderRadius: BorderRadius.circular(14)),
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        ClipRRect(
          borderRadius: const BorderRadius.only(topLeft: Radius.circular(14), topRight: Radius.circular(14)),
          child: product['imageUrl'] != null
              ? Image.network(product['imageUrl'] as String,
                  height: 110, width: double.infinity, fit: BoxFit.cover,
                  errorBuilder: (_, _, _) => Container(height: 110, color: const Color(0xFF2A2A2A),
                      child: const Icon(Icons.image_not_supported, color: Colors.white24)))
              : Container(height: 110, color: const Color(0xFF2A2A2A),
                  child: const Icon(Icons.shopping_bag_outlined, color: Colors.white24, size: 40)),
        ),
        Padding(
          padding: const EdgeInsets.all(10),
          child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text(product['name'] as String? ?? '', maxLines: 2, overflow: TextOverflow.ellipsis,
                style: const TextStyle(color: Colors.white, fontSize: 13, fontWeight: FontWeight.w600)),
            const SizedBox(height: 4),
            Text('\$${product['price']}', style: const TextStyle(color: Colors.white70, fontSize: 13)),
            const SizedBox(height: 6),
            Row(children: [
              Text(stock > 0 ? '$stock left' : 'Out of stock',
                  style: TextStyle(color: stock > 0 ? Colors.green : Colors.red, fontSize: 11)),
              const Spacer(),
              GestureDetector(
                onTap: stock > 0 ? onAddToCart : null,
                child: Container(
                  width: 28, height: 28,
                  decoration: BoxDecoration(
                    color: stock > 0 ? Colors.white : const Color(0xFF3A3A3A),
                    shape: BoxShape.circle,
                  ),
                  child: Icon(Icons.add, color: stock > 0 ? Colors.black : Colors.white24, size: 16),
                ),
              ),
            ]),
          ]),
        ),
      ]),
    );
  }
}
