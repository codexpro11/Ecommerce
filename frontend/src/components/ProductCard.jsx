import { Link } from 'react-router-dom';
import { getProductImageUrl } from '../api/client';
import { useCart } from '../context/CartContext';
import './ProductCard.css';

export default function ProductCard({ product, onWishlistToggle, inWishlist }) {
  const { dispatch } = useCart();
  const imageUrl = product.id ? getProductImageUrl(product.id) : null;

  const addToCart = (e) => {
    e.preventDefault();
    dispatch({
      type: 'ADD',
      item: { productId: product.id, productName: product.productName, price: product.price, quantity: 1 },
    });
  };

  return (
    <article className="product-card card">
      <Link to={`/product/${product.id}`} className="product-card-link">
        <div className="product-card-image-wrap">
          {imageUrl ? (
            <img src={imageUrl} alt={product.productName} className="product-card-image" />
          ) : (
            <div className="product-card-placeholder">No image</div>
          )}
          {onWishlistToggle && (
            <button
              type="button"
              className={`wishlist-btn ${inWishlist ? 'active' : ''}`}
              onClick={(e) => {
                e.preventDefault();
                onWishlistToggle({ productId: product.id, productName: product.productName });
              }}
              aria-label={inWishlist ? 'Remove from wishlist' : 'Add to wishlist'}
            >
              ♥
            </button>
          )}
        </div>
        <div className="product-card-body">
          <span className="product-card-category">{product.category}</span>
          <h3 className="product-card-title">{product.productName}</h3>
          {product.brand && <span className="product-card-brand">{product.brand}</span>}
          <p className="product-card-price">₹{product.price?.toLocaleString()}</p>
        </div>
      </Link>
      <div className="product-card-actions">
        <button type="button" className="btn btn-primary btn-sm" onClick={addToCart}>
          Add to cart
        </button>
      </div>
    </article>
  );
}
