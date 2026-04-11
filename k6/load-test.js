import http from 'k6/http';
import { check, sleep } from 'k6';

// k6 configuration
export const options = {
    stages: [
        { duration: '30s', target: 50 },  // Ramp-up to 50 users over 30 seconds
        { duration: '1m', target: 50 },   // Stay at 50 users for 1 minute
        { duration: '10s', target: 0 },   // Ramp-down to 0 users
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'], // 95% of requests should be below 500ms
        http_req_failed: ['rate<0.01'],   // Error rate should be less than 1%
    },
};

export default function () {
    const url = 'http://localhost:8081/orders';
    
    const payload = JSON.stringify({
        customerId: `CUST-${Math.floor(Math.random() * 10000)}`,
        // Occasional $999.99 to simulate failures and trigger DLQ
        amount: Math.random() < 0.05 ? 999.99 : parseFloat((Math.random() * 500).toFixed(2)) 
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const res = http.post(url, payload, params);

    check(res, {
        'is status 200': (r) => r.status === 200,
        'has order id': (r) => JSON.parse(r.body).orderId !== undefined,
    });

    sleep(1); // Think time
}
