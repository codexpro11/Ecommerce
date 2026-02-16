import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getProductById, getProductImageUrl, getWishlist, toggleWishlist } from '../api/client';
import { useCart } from '../context/CartContext';
import './ProductDetail.css';

export default function ProductDetail() {
  const { id } = useParams();
  const { dispatch } = useCart();
  const [product, setProduct] = useState(null);
  const [wishlist, setWishlist] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [quantity, setQuantity] = useState(1);

  useEffect(() => {
    getProductById(id)
      .then(setProduct)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
    getWishlist().then(setWishlist).catch(() => setWishlist([]));
  }, [id]);

  const inWishlist = product && wishlist.some((w) => w.productId === product.id);

  const handleWishlistToggle = () => {
    if (!product) return;
    toggleWishlist({ productId: product.id, productName: product.productName }).then(() =>
      getWishlist().then(setWishlist)
    );
  };

  const addToCart = () => {
    if (!product) return;
    dispatch({
      type: 'ADD',
      item: {
        productId: product.id,
        productName: product.productName,
        price: product.price,
        quantity,
      },
    });
  };

  if (loading) return <div className="container product-detail"><p className="loading">Loading…</p></div>;
  if (error || !product) return <div className="container product-detail"><p className="error">{error || 'Product not found.'}</p><Link to="/products">Back to products</Link></div>;

  const imageUrl = getProductImageUrl(product.id);

  return (
    <div className="container product-detail">
      <Link to="/products" className="back-link">← Back to products</Link>
      <div className="product-detail-grid">
        <div className="product-detail-image-wrap card">
          <img src={imageUrl} alt={product.productName} className="product-detail-image" />
        </div>
        <div className="product-detail-info">
          <span className="product-detail-category">{product.category}</span>
          <h1 className="product-detail-title">{product.productName}</h1>
          {product.brand && <p className="product-detail-brand">{product.brand}</p>}
          <p className="product-detail-price">₹{product.price?.toLocaleString()}</p>
          {product.description && <p className="product-detail-desc">{product.description}</p>}
          {product.stockQuantity != null && (
            <p className="product-detail-stock">In stock: {product.stockQuantity}</p>
          )}
          <div className="product-detail-actions">
            <div className="quantity-wrap">
              <label htmlFor="qty">Quantity</label>
              <input
                id="qty"
                type="number"
                min={1}
                max={product.stockQuantity ?? 99}
                value={quantity}
                onChange={(e) => setQuantity(Math.max(1, parseInt(e.target.value, 10) || 1))}
              />
            </div>
            <button type="button" className="btn btn-primary" onClick={addToCart}>
              Add to cart
            </button>
            <button
              type="button"
              className={`btn btn-secondary ${inWishlist ? 'active' : ''}`}
              onClick={handleWishlistToggle}
            >
              {inWishlist ? '♥ In wishlist' : '♥ Add to wishlist'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
