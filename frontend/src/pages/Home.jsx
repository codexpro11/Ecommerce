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
          <h1 className="hero-title">Welcome to ProductSite</h1>
          <p className="hero-subtitle">
            Discover products you’ll love. Browse, add to cart, and checkout in one place.
          </p>
          <Link to="/products" className="btn btn-primary hero-cta">
            Shop all products
          </Link>
        </div>
      </section>

      <section className="section featured">
        <div className="container">
          <h2 className="section-title">Featured products</h2>
          {loading && <p className="loading">Loading products…</p>}
          {error && <p className="error">Error: {error}</p>}
          {!loading && !error && (
            <div className="product-grid">
              {featured.map((p) => (
                <ProductCard key={p.id} product={p} />
              ))}
            </div>
          )}
          {!loading && !error && products.length > 8 && (
            <div className="section-cta">
              <Link to="/products" className="btn btn-secondary">
                View all products
              </Link>
            </div>
          )}
        </div>
      </section>
    </div>
  );
}
