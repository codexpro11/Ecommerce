import { Link } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import Chatbot from './Chatbot';
import './Layout.css';

export default function Layout({ children }) {
  const { cart } = useCart();
  const cartCount = cart.reduce((sum, i) => sum + (i.quantity || 0), 0);

  return (
    <div className="layout">
      <header className="header">
        <div className="container header-inner">
          <Link to="/" className="logo">
            ProductSite
          </Link>
          <nav className="nav">
            <Link to="/">Home</Link>
            <Link to="/products">Products</Link>
            <Link to="/add-product">Add Product</Link>
            <Link to="/wishlist">Wishlist</Link>
            <Link to="/cart" className="cart-link">
              Cart
              {cartCount > 0 && <span className="badge">{cartCount}</span>}
            </Link>
          </nav>
        </div>
      </header>
      <main className="main">{children}</main>
      <footer className="footer">
        <div className="container">
          <p>© ProductSite. Built with Spring Boot & React.</p>
        </div>
      </footer>
      <Chatbot />
    </div>
  );
}
