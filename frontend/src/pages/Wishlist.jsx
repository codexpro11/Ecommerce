import { useEffect, useState } from 'react';
import { getWishlist, toggleWishlist, getProductById } from '../api/client';
import ProductCard from '../components/ProductCard';
import './Wishlist.css';

export default function Wishlist() {
  const [wishlist, setWishlist] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const loadWishlist = () => {
    getWishlist()
      .then(setWishlist)
      .catch((e) => {
        setError(e.message);
        setWishlist([]);
      });
  };

  useEffect(() => {
    loadWishlist();
  }, []);

  useEffect(() => {
    if (wishlist.length === 0) {
      setProducts([]);
      setLoading(false);
      return;
    }
    setLoading(true);
    Promise.all(wishlist.map((w) => getProductById(w.productId)))
      .then(setProducts)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [wishlist]);

  const handleWishlistToggle = (item) => {
    toggleWishlist(item).then(() => loadWishlist());
  };

  const wishlistIds = new Set(wishlist.map((w) => w.productId));

  return (
    <div className="container wishlist-page">
      <h1 className="page-title">Wishlist</h1>
      {loading && <p className="loading">Loading…</p>}
      {error && <p className="error">Error: {error}</p>}
      {!loading && !error && products.length === 0 && (
        <p className="empty">Your wishlist is empty. Add products from the shop.</p>
      )}
      {!loading && !error && products.length > 0 && (
        <div className="product-grid">
          {products.map((p) => (
            <ProductCard
              key={p.id}
              product={p}
              onWishlistToggle={handleWishlistToggle}
              inWishlist={wishlistIds.has(p.id)}
            />
          ))}
        </div>
      )}
    </div>
  );
}
