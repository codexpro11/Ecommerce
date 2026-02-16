import React from 'react';
import WishlistButton from './WishlistButton';

export default function ProductCard({ product }) {
  return (
    <div style={{ 
      position: 'relative', 
      width: '300px', 
      border: '1px solid #ddd', 
      borderRadius: '12px',
      overflow: 'hidden'
    }}>
      
      {/* Product Image */}
      <img src={product.image} alt={product.productName} style={{ width: '100%', height: '200px', objectFit: 'cover' }} />

      {/* The Floating Wishlist Button */}
      <div style={{ position: 'absolute', top: '10px', right: '10px' }}>
        <WishlistButton productId={product.id} />
      </div>

      {/* Product Info */}
      <div style={{ padding: '16px' }}>
        <h3>{product.ProductName}</h3>
        <p>${product.price}</p>
      </div>
    </div>
  );
}