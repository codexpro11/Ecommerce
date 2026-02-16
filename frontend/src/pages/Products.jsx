import { useEffect, useState } from 'react';
import { getProducts, searchProducts } from '../api/client';
import { getWishlist, toggleWishlist } from '../api/client';
import ProductCard from '../components/ProductCard';
import './Products.css';

export default function Products() {
  const [products, setProducts] = useState([]);
  const [wishlist, setWishlist] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [error, setError] = useState(null);

  const loadProducts = () => {
    setLoading(true);
    setError(null);
    getProducts()
      .then(setProducts)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  };

  const loadWishlist = () => {
    getWishlist().then(setWishlist).catch(() => setWishlist([]));
  };

  useEffect(() => {
    loadProducts();
    loadWishlist();
  }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    if (!search.trim()) {
      loadProducts();
      return;
    }
    setLoading(true);
    setError(null);
    searchProducts(search.trim())
      .then(setProducts)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  };

  const handleWishlistToggle = (item) => {
    toggleWishlist(item).then(() => loadWishlist());
  };

  const wishlistIds = new Set(wishlist.map((w) => w.productId));

  return (
    <div className="products-page container">
      <h1 className="page-title">Products</h1>
      <form className="search-form" onSubmit={handleSearch}>
        <input
          type="search"
          placeholder="Search products…"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="search-input"
        />
        <button type="submit" className="btn btn-primary">
          Search
        </button>
      </form>

      {loading && <p className="loading">Loading…</p>}
      {error && <p className="error">Error: {error}</p>}
      {!loading && !error && (
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
      {!loading && !error && products.length === 0 && (
        <p className="empty">No products found.</p>
      )}
    </div>
  );
}
