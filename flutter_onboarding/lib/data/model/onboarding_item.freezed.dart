// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides

part of 'onboarding_item.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more informations: https://github.com/rrousselGit/freezed#custom-getters-and-methods');

/// @nodoc
class _$OnboardingItemTearOff {
  const _$OnboardingItemTearOff();

  _OnboardingItem call(
      {required String title,
      required String subtitle,
      required String imagePath}) {
    return _OnboardingItem(
      title: title,
      subtitle: subtitle,
      imagePath: imagePath,
    );
  }
}

/// @nodoc
const $OnboardingItem = _$OnboardingItemTearOff();

/// @nodoc
mixin _$OnboardingItem {
  String get title => throw _privateConstructorUsedError;
  String get subtitle => throw _privateConstructorUsedError;
  String get imagePath => throw _privateConstructorUsedError;

  @JsonKey(ignore: true)
  $OnboardingItemCopyWith<OnboardingItem> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $OnboardingItemCopyWith<$Res> {
  factory $OnboardingItemCopyWith(
          OnboardingItem value, $Res Function(OnboardingItem) then) =
      _$OnboardingItemCopyWithImpl<$Res>;
  $Res call({String title, String subtitle, String imagePath});
}

/// @nodoc
class _$OnboardingItemCopyWithImpl<$Res>
    implements $OnboardingItemCopyWith<$Res> {
  _$OnboardingItemCopyWithImpl(this._value, this._then);

  final OnboardingItem _value;
  // ignore: unused_field
  final $Res Function(OnboardingItem) _then;

  @override
  $Res call({
    Object? title = freezed,
    Object? subtitle = freezed,
    Object? imagePath = freezed,
  }) {
    return _then(_value.copyWith(
      title: title == freezed
          ? _value.title
          : title // ignore: cast_nullable_to_non_nullable
              as String,
      subtitle: subtitle == freezed
          ? _value.subtitle
          : subtitle // ignore: cast_nullable_to_non_nullable
              as String,
      imagePath: imagePath == freezed
          ? _value.imagePath
          : imagePath // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc
abstract class _$OnboardingItemCopyWith<$Res>
    implements $OnboardingItemCopyWith<$Res> {
  factory _$OnboardingItemCopyWith(
          _OnboardingItem value, $Res Function(_OnboardingItem) then) =
      __$OnboardingItemCopyWithImpl<$Res>;
  @override
  $Res call({String title, String subtitle, String imagePath});
}

/// @nodoc
class __$OnboardingItemCopyWithImpl<$Res>
    extends _$OnboardingItemCopyWithImpl<$Res>
    implements _$OnboardingItemCopyWith<$Res> {
  __$OnboardingItemCopyWithImpl(
      _OnboardingItem _value, $Res Function(_OnboardingItem) _then)
      : super(_value, (v) => _then(v as _OnboardingItem));

  @override
  _OnboardingItem get _value => super._value as _OnboardingItem;

  @override
  $Res call({
    Object? title = freezed,
    Object? subtitle = freezed,
    Object? imagePath = freezed,
  }) {
    return _then(_OnboardingItem(
      title: title == freezed
          ? _value.title
          : title // ignore: cast_nullable_to_non_nullable
              as String,
      subtitle: subtitle == freezed
          ? _value.subtitle
          : subtitle // ignore: cast_nullable_to_non_nullable
              as String,
      imagePath: imagePath == freezed
          ? _value.imagePath
          : imagePath // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc
class _$_OnboardingItem
    with DiagnosticableTreeMixin
    implements _OnboardingItem {
  _$_OnboardingItem(
      {required this.title, required this.subtitle, required this.imagePath});

  @override
  final String title;
  @override
  final String subtitle;
  @override
  final String imagePath;

  @override
  String toString({DiagnosticLevel minLevel = DiagnosticLevel.info}) {
    return 'OnboardingItem(title: $title, subtitle: $subtitle, imagePath: $imagePath)';
  }

  @override
  void debugFillProperties(DiagnosticPropertiesBuilder properties) {
    super.debugFillProperties(properties);
    properties
      ..add(DiagnosticsProperty('type', 'OnboardingItem'))
      ..add(DiagnosticsProperty('title', title))
      ..add(DiagnosticsProperty('subtitle', subtitle))
      ..add(DiagnosticsProperty('imagePath', imagePath));
  }

  @override
  bool operator ==(dynamic other) {
    return identical(this, other) ||
        (other is _OnboardingItem &&
            (identical(other.title, title) ||
                const DeepCollectionEquality().equals(other.title, title)) &&
            (identical(other.subtitle, subtitle) ||
                const DeepCollectionEquality()
                    .equals(other.subtitle, subtitle)) &&
            (identical(other.imagePath, imagePath) ||
                const DeepCollectionEquality()
                    .equals(other.imagePath, imagePath)));
  }

  @override
  int get hashCode =>
      runtimeType.hashCode ^
      const DeepCollectionEquality().hash(title) ^
      const DeepCollectionEquality().hash(subtitle) ^
      const DeepCollectionEquality().hash(imagePath);

  @JsonKey(ignore: true)
  @override
  _$OnboardingItemCopyWith<_OnboardingItem> get copyWith =>
      __$OnboardingItemCopyWithImpl<_OnboardingItem>(this, _$identity);
}

abstract class _OnboardingItem implements OnboardingItem {
  factory _OnboardingItem(
      {required String title,
      required String subtitle,
      required String imagePath}) = _$_OnboardingItem;

  @override
  String get title => throw _privateConstructorUsedError;
  @override
  String get subtitle => throw _privateConstructorUsedError;
  @override
  String get imagePath => throw _privateConstructorUsedError;
  @override
  @JsonKey(ignore: true)
  _$OnboardingItemCopyWith<_OnboardingItem> get copyWith =>
      throw _privateConstructorUsedError;
}
