import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getProducts } from '../api/client';
import ProductCard from '../components/ProductCard';
import './Home.css';

export default function Home() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    getProducts()
      .then(setProducts)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  const featured = products.slice(0, 8);

  return (
    <div className="home">
      <section className="hero">
        <div className="container hero-inner">
          <span className="hero-badge">✨ AI-Powered Product Descriptions</span>
          <h1 className="hero-title">Discover Products You'll Love</h1>
          <p className="hero-subtitle">
            Browse curated collections, add to your cart, and enjoy a seamless shopping experience — all in one place.
          </p>
          <Link to="/products" className="btn btn-primary hero-cta">
            Explore Products →
          </Link>
          <div className="hero-stats">
            <div className="hero-stat">
              <span className="hero-stat-value">{products.length || '—'}</span>
              <span className="hero-stat-label">Products</span>
            </div>
            <div className="hero-stat">
              <span className="hero-stat-value">AI</span>
              <span className="hero-stat-label">Descriptions</span>
            </div>
            <div className="hero-stat">
              <span className="hero-stat-value">24/7</span>
              <span className="hero-stat-label">Chat Support</span>
            </div>
          </div>
        </div>
      </section>

      <section className="section featured">
        <div className="container">
          <div className="section-header">
            <div>
              <h2 className="section-title">Featured Products</h2>
              <p className="section-subtitle">Hand-picked for you</p>
            </div>
            {!loading && !error && products.length > 8 && (
              <Link to="/products" className="btn btn-secondary">
                View all →
              </Link>
            )}
          </div>
          {loading && <p className="loading">Loading products…</p>}
          {error && <p className="error">Error: {error}</p>}
          {!loading && !error && (
            <div className="product-grid">
              {featured.map((p) => (
                <ProductCard key={p.id} product={p} />
              ))}
            </div>
          )}
        </div>
      </section>
    </div>
  );
}
