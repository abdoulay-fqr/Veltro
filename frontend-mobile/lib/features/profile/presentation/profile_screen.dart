import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';
import 'profile_notifier.dart';

class ProfileScreen extends ConsumerStatefulWidget {
  final int memberProfileId;
  const ProfileScreen({super.key, required this.memberProfileId});

  @override
  ConsumerState<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends ConsumerState<ProfileScreen> {
  final _firstnameCtrl = TextEditingController();
  final _lastnameCtrl = TextEditingController();
  final _phoneCtrl = TextEditingController();
  final _weightCtrl = TextEditingController();
  final _heightCtrl = TextEditingController();
  final _objectiveCtrl = TextEditingController();
  final _restrictionsCtrl = TextEditingController();

  bool _populated = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      ref.read(profileNotifierProvider.notifier).load(widget.memberProfileId);
    });
  }

  void _populateFields(Map<String, dynamic> member, Map<String, dynamic>? health) {
    if (_populated) return;
    _firstnameCtrl.text = member['firstname'] ?? '';
    _lastnameCtrl.text = member['lastname'] ?? '';
    _phoneCtrl.text = member['phone'] ?? '';
    if (health != null) {
      _weightCtrl.text = health['weightKg']?.toString() ?? '';
      _heightCtrl.text = health['heightCm']?.toString() ?? '';
      _objectiveCtrl.text = health['fitnessObjective'] ?? '';
      _restrictionsCtrl.text = health['medicalRestrictions'] ?? '';
    }
    _populated = true;
  }

  Future<void> _pickAvatar() async {
    final picker = ImagePicker();
    final picked = await picker.pickImage(source: ImageSource.gallery, maxWidth: 800, imageQuality: 85);
    if (picked == null) return;
    final success = await ref
        .read(profileNotifierProvider.notifier)
        .uploadAvatar(widget.memberProfileId, picked.path);
    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(success ? 'Avatar updated' : 'Failed to upload avatar')),
      );
    }
  }

  Future<void> _saveProfile() async {
    final success = await ref.read(profileNotifierProvider.notifier).updateProfile(
      widget.memberProfileId,
      {
        'firstname': _firstnameCtrl.text.trim(),
        'lastname': _lastnameCtrl.text.trim(),
        'phone': _phoneCtrl.text.trim(),
      },
    );
    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(success ? 'Profile updated' : 'Failed to save profile')),
      );
    }
  }

  Future<void> _saveHealth() async {
    final success = await ref.read(profileNotifierProvider.notifier).upsertHealthProfile(
      widget.memberProfileId,
      {
        'fitnessObjective': _objectiveCtrl.text.trim(),
        'medicalRestrictions': _restrictionsCtrl.text.trim(),
        if (_weightCtrl.text.isNotEmpty) 'weightKg': double.tryParse(_weightCtrl.text),
        if (_heightCtrl.text.isNotEmpty) 'heightCm': double.tryParse(_heightCtrl.text),
      },
    );
    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(success ? 'Health profile updated' : 'Failed to save health profile')),
      );
    }
  }

  @override
  void dispose() {
    _firstnameCtrl.dispose();
    _lastnameCtrl.dispose();
    _phoneCtrl.dispose();
    _weightCtrl.dispose();
    _heightCtrl.dispose();
    _objectiveCtrl.dispose();
    _restrictionsCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(profileNotifierProvider);

    if (state.member != null) _populateFields(state.member!, state.healthProfile);

    return Scaffold(
      backgroundColor: const Color(0xFF0F0F0F),
      appBar: AppBar(
        backgroundColor: const Color(0xFF0F0F0F),
        foregroundColor: Colors.white,
        title: const Text('My Profile', style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600)),
        elevation: 0,
        actions: [
          if (state.isSaving)
            const Padding(
              padding: EdgeInsets.all(16),
              child: SizedBox(width: 16, height: 16, child: CircularProgressIndicator(strokeWidth: 2)),
            ),
        ],
      ),
      body: state.isLoading
          ? const Center(child: CircularProgressIndicator())
          : SingleChildScrollView(
              padding: const EdgeInsets.all(20),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Avatar
                  Center(
                    child: GestureDetector(
                      onTap: _pickAvatar,
                      child: Stack(
                        children: [
                          CircleAvatar(
                            radius: 48,
                            backgroundColor: const Color(0xFF1E1E1E),
                            backgroundImage: state.member?['avatarUrl'] != null
                                ? NetworkImage('http://localhost:8082${state.member!['avatarUrl']}')
                                : null,
                            child: state.member?['avatarUrl'] == null
                                ? Text(
                                    (state.member?['firstname'] ?? 'M')[0],
                                    style: const TextStyle(fontSize: 32, color: Colors.white54),
                                  )
                                : null,
                          ),
                          Positioned(
                            bottom: 0,
                            right: 0,
                            child: Container(
                              width: 28,
                              height: 28,
                              decoration: BoxDecoration(
                                color: Colors.white,
                                shape: BoxShape.circle,
                                border: Border.all(color: const Color(0xFF0F0F0F), width: 2),
                              ),
                              child: const Icon(Icons.camera_alt, size: 14, color: Colors.black),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 28),

                  // Profile section
                  _SectionHeader(title: 'Personal Info'),
                  const SizedBox(height: 12),
                  _TextField(controller: _firstnameCtrl, label: 'First Name'),
                  const SizedBox(height: 12),
                  _TextField(controller: _lastnameCtrl, label: 'Last Name'),
                  const SizedBox(height: 12),
                  _TextField(controller: _phoneCtrl, label: 'Phone', keyboardType: TextInputType.phone),
                  const SizedBox(height: 16),
                  _SaveButton(label: 'Save Personal Info', onPressed: _saveProfile),
                  const SizedBox(height: 28),

                  // Health section
                  _SectionHeader(title: 'Health Profile'),
                  const SizedBox(height: 12),
                  _TextField(controller: _objectiveCtrl, label: 'Fitness Objective (e.g. WEIGHT_LOSS)'),
                  const SizedBox(height: 12),
                  _TextField(controller: _weightCtrl, label: 'Weight (kg)', keyboardType: TextInputType.number),
                  const SizedBox(height: 12),
                  _TextField(controller: _heightCtrl, label: 'Height (cm)', keyboardType: TextInputType.number),
                  const SizedBox(height: 12),
                  _TextField(controller: _restrictionsCtrl, label: 'Medical Restrictions', maxLines: 3),
                  const SizedBox(height: 16),
                  _SaveButton(label: 'Save Health Profile', onPressed: _saveHealth),
                  const SizedBox(height: 32),
                ],
              ),
            ),
    );
  }
}

class _SectionHeader extends StatelessWidget {
  final String title;
  const _SectionHeader({required this.title});

  @override
  Widget build(BuildContext context) => Text(
        title,
        style: const TextStyle(
          color: Colors.white,
          fontSize: 14,
          fontWeight: FontWeight.w600,
          letterSpacing: 0.3,
        ),
      );
}

class _TextField extends StatelessWidget {
  final TextEditingController controller;
  final String label;
  final TextInputType? keyboardType;
  final int maxLines;

  const _TextField({
    required this.controller,
    required this.label,
    this.keyboardType,
    this.maxLines = 1,
  });

  @override
  Widget build(BuildContext context) => TextField(
        controller: controller,
        keyboardType: keyboardType,
        maxLines: maxLines,
        style: const TextStyle(color: Colors.white, fontSize: 14),
        decoration: InputDecoration(
          labelText: label,
          labelStyle: const TextStyle(color: Colors.white38, fontSize: 13),
          filled: true,
          fillColor: const Color(0xFF1E1E1E),
          border: OutlineInputBorder(
            borderRadius: BorderRadius.circular(12),
            borderSide: BorderSide.none,
          ),
          focusedBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(12),
            borderSide: const BorderSide(color: Colors.white24),
          ),
        ),
      );
}

class _SaveButton extends StatelessWidget {
  final String label;
  final VoidCallback onPressed;
  const _SaveButton({required this.label, required this.onPressed});

  @override
  Widget build(BuildContext context) => SizedBox(
        width: double.infinity,
        height: 48,
        child: ElevatedButton(
          onPressed: onPressed,
          style: ElevatedButton.styleFrom(
            backgroundColor: Colors.white,
            foregroundColor: Colors.black,
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
          ),
          child: Text(label, style: const TextStyle(fontSize: 14, fontWeight: FontWeight.w600)),
        ),
      );
}
