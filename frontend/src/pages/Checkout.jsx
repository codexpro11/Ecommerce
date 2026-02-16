import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { placeOrder } from '../api/client';
import './Checkout.css';

export default function Checkout() {
  const { cart, dispatch } = useCart();
  const navigate = useNavigate();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const total = cart.reduce((sum, i) => sum + (i.price || 0) * (i.quantity || 0), 0);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (cart.length === 0) return;
    setSubmitting(true);
    setError(null);
    const orderRequest = {
      customerName: name.trim(),
      email: email.trim(),
      items: cart.map((i) => ({
        productId: i.productId,
        quality: i.quantity,
      })),
    };
    placeOrder(orderRequest)
      .then(() => {
        dispatch({ type: 'CLEAR' });
        navigate('/');
      })
      .catch((e) => {
        setError(e.message);
        setSubmitting(false);
      });
  };

  if (cart.length === 0 && !submitting) {
    return (
      <div className="container checkout-page">
        <h1 className="page-title">Checkout</h1>
        <p className="empty-cart">Your cart is empty.</p>
        <Link to="/products" className="btn btn-primary">Shop products</Link>
      </div>
    );
  }

  return (
    <div className="container checkout-page">
      <h1 className="page-title">Checkout</h1>
      <form className="checkout-form card" onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="name">Name</label>
          <input
            id="name"
            type="text"
            required
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Your name"
          />
        </div>
        <div className="form-group">
          <label htmlFor="email">Email</label>
          <input
            id="email"
            type="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="your@email.com"
          />
        </div>
        <div className="order-summary">
          <h3>Order summary</h3>
          <ul>
            {cart.map((i) => (
              <li key={i.productId}>
                {i.productName} × {i.quantity} — ₹{(i.price * i.quantity).toLocaleString()}
              </li>
            ))}
          </ul>
          <p className="order-total">Total: ₹{total.toLocaleString()}</p>
        </div>
        {error && <p className="form-error">{error}</p>}
        <button type="submit" className="btn btn-primary btn-block" disabled={submitting}>
          {submitting ? 'Placing order…' : 'Place order'}
        </button>
      </form>
    </div>
  );
}
