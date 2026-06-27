import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getAllOrders } from '../api/client';
import './Orders.css';

export default function Orders() {
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [expandedOrder, setExpandedOrder] = useState(null);

    useEffect(() => {
        getAllOrders()
            .then(setOrders)
            .catch((e) => setError(e.message))
            .finally(() => setLoading(false));
    }, []);

    const toggleOrder = (orderId) => {
        setExpandedOrder(expandedOrder === orderId ? null : orderId);
    };

    const getStatusClass = (status) => {
        if (!status) return '';
        switch (status.toLowerCase()) {
            case 'placed': return 'status-placed';
            case 'shipped': return 'status-shipped';
            case 'delivered': return 'status-delivered';
            case 'cancelled': return 'status-cancelled';
            default: return 'status-placed';
        }
    };

    const getTotal = (items) => {
        if (!items || items.length === 0) return 0;
        return items.reduce((sum, item) => sum + (parseFloat(item.totalPrice) || 0), 0);
    };

    if (loading) {
        return (
            <div className="container orders-page">
                <h1 className="page-title">My Orders</h1>
                <div className="card" style={{ textAlign: 'center', padding: '3rem' }}>
                    <p style={{ color: 'var(--text-secondary)' }}>Loading orders…</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="container orders-page">
                <h1 className="page-title">My Orders</h1>
                <div className="card" style={{ textAlign: 'center', padding: '3rem' }}>
                    <p className="error">{error}</p>
                </div>
            </div>
        );
    }

    return (
        <div className="container orders-page">
            <h1 className="page-title">My Orders</h1>
            <Link to="/products" className="back-link">← Continue shopping</Link>

            {orders.length === 0 ? (
                <div className="card orders-empty">
                    <p>📦 No orders yet</p>
                    <Link to="/products" className="btn btn-primary">Start shopping</Link>
                </div>
            ) : (
                <div className="orders-list">
                    {orders.map((order) => (
                        <div key={order.orderId} className="order-card card">
                            <div className="order-header" onClick={() => toggleOrder(order.orderId)}>
                                <div className="order-header-left">
                                    <span className="order-id">#{order.orderId}</span>
                                    <span className={`order-status ${getStatusClass(order.status)}`}>
                                        {order.status || 'Placed'}
                                    </span>
                                </div>
                                <div className="order-header-right">
                                    <span className="order-date">
                                        {order.orderDate ? new Date(order.orderDate).toLocaleDateString('en-IN', {
                                            year: 'numeric', month: 'short', day: 'numeric'
                                        }) : '—'}
                                    </span>
                                    <span className="order-total">₹{getTotal(order.items).toLocaleString()}</span>
                                    <span className={`order-expand ${expandedOrder === order.orderId ? 'expanded' : ''}`}>▸</span>
                                </div>
                            </div>

                            {expandedOrder === order.orderId && (
                                <div className="order-details">
                                    <div className="order-customer">
                                        <p><strong>Customer:</strong> {order.customerName || '—'}</p>
                                        <p><strong>Email:</strong> {order.email || '—'}</p>
                                    </div>

                                    <table className="order-items-table">
                                        <thead>
                                            <tr>
                                                <th>Product</th>
                                                <th>Qty</th>
                                                <th>Total</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {order.items?.map((item, idx) => (
                                                <tr key={idx}>
                                                    <td>{item.productName}</td>
                                                    <td>{item.quantity}</td>
                                                    <td>₹{parseFloat(item.totalPrice).toLocaleString()}</td>
                                                </tr>
                                            ))}
                                        </tbody>
                                        <tfoot>
                                            <tr>
                                                <td colSpan="2"><strong>Order Total</strong></td>
                                                <td><strong>₹{getTotal(order.items).toLocaleString()}</strong></td>
                                            </tr>
                                        </tfoot>
                                    </table>
                                </div>
                            )}
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
