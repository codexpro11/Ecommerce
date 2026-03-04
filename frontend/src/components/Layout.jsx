import { Link, useLocation } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import Chatbot from './Chatbot';
import './Layout.css';

export default function Layout({ children }) {
  const { cart } = useCart();
  const location = useLocation();
  const cartCount = cart.reduce((sum, i) => sum + (i.quantity || 0), 0);

  const navLinks = [
    { to: '/', label: 'Home' },
    { to: '/products', label: 'Products' },
    { to: '/add-product', label: 'Add Product' },
    { to: '/wishlist', label: 'Wishlist' },
    { to: '/orders', label: 'Orders' },
  ];

  return (
    <div className="layout">
      <header className="header">
        <div className="container header-inner">
          <Link to="/" className="logo">
            ✦ ProductSite
          </Link>
          <nav className="nav">
            {navLinks.map((link) => (
              <Link
                key={link.to}
                to={link.to}
                className={location.pathname === link.to ? 'active' : ''}
              >
                {link.label}
              </Link>
            ))}
            <Link to="/cart" className="cart-link">
              🛒 Cart
              {cartCount > 0 && <span className="badge">{cartCount}</span>}
            </Link>
          </nav>
        </div>
      </header>
      <main className="main">{children}</main>
      <footer className="footer">
        <div className="container">
          <div className="footer-inner">
            <div className="footer-brand">
              <span className="footer-brand-name">✦ ProductSite</span>
              <p>Your one-stop destination for amazing products. Discover, shop, and enjoy a seamless e-commerce experience.</p>
            </div>
            <div className="footer-col">
              <h4>Shop</h4>
              <Link to="/products">All Products</Link>
              <Link to="/wishlist">Wishlist</Link>
              <Link to="/cart">Cart</Link>
            </div>
            <div className="footer-col">
              <h4>Seller</h4>
              <Link to="/add-product">Add Product</Link>
            </div>
            <div className="footer-col">
              <h4>Support</h4>
              <a href="#" onClick={(e) => e.preventDefault()}>Help Center</a>
              <a href="#" onClick={(e) => e.preventDefault()}>Shipping Info</a>
              <a href="#" onClick={(e) => e.preventDefault()}>Returns</a>
            </div>
          </div>
          <div className="footer-bottom">
            <p>© {new Date().getFullYear()} ProductSite. Built with Spring Boot & React.</p>
            <div className="footer-socials">
              <a href="#" aria-label="Twitter" onClick={(e) => e.preventDefault()}>𝕏</a>
              <a href="#" aria-label="GitHub" onClick={(e) => e.preventDefault()}>⌘</a>
              <a href="#" aria-label="Email" onClick={(e) => e.preventDefault()}>✉</a>
            </div>
          </div>
        </div>
      </footer>
      <Chatbot />
    </div>
  );
}
