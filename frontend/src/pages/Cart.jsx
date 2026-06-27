import { Link } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { getProductImageUrl } from '../api/client';
import './Cart.css';

export default function Cart() {
  const { cart, dispatch } = useCart();
  const total = cart.reduce((sum, i) => sum + (i.price || 0) * (i.quantity || 0), 0);

  if (cart.length === 0) {
    return (
      <div className="container cart-page">
        <h1 className="page-title">Cart</h1>
        <p className="empty-cart">Your cart is empty.</p>
        <Link to="/products" className="btn btn-primary">Shop products</Link>
      </div>
    );
  }

  return (
    <div className="container cart-page">
      <h1 className="page-title">Cart</h1>
      <div className="cart-list">
        {cart.map((item) => (
          <div key={item.productId} className="cart-item card">
            <div className="cart-item-image">
              <img src={getProductImageUrl(item.productId)} alt={item.productName} />
            </div>
            <div className="cart-item-details">
              <Link to={`/product/${item.productId}`} className="cart-item-name">
                {item.productName}
              </Link>
              <p className="cart-item-price">₹{(item.price * item.quantity).toLocaleString()}</p>
              <div className="cart-item-qty">
                <button
                  type="button"
                  className="btn btn-ghost qty-btn"
                  onClick={() =>
                    dispatch({
                      type: 'SET_QUANTITY',
                      productId: item.productId,
                      quantity: Math.max(0, (item.quantity || 1) - 1),
                    })
                  }
                >
                  −
                </button>
                <span>{item.quantity}</span>
                <button
                  type="button"
                  className="btn btn-ghost qty-btn"
                  onClick={() =>
                    dispatch({
                      type: 'SET_QUANTITY',
                      productId: item.productId,
                      quantity: (item.quantity || 1) + 1,
                    })
                  }
                >
                  +
                </button>
              </div>
            </div>
            <button
              type="button"
              className="btn btn-ghost remove-btn"
              onClick={() => dispatch({ type: 'REMOVE', productId: item.productId })}
            >
              Remove
            </button>
          </div>
        ))}
      </div>
      <div className="cart-summary card">
        <h2>Summary</h2>
        <p className="cart-total">Total: ₹{total.toLocaleString()}</p>
        <Link to="/checkout" className="btn btn-primary btn-block">
          Proceed to checkout
        </Link>
      </div>
    </div>
  );
}
