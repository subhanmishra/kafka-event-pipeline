import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  scenarios: {
    orders_load: {
      executor: 'constant-arrival-rate',
      rate: 30,              // 10 new orders per second
      timeUnit: '1s',
      duration: '10m',       // ~12,000 orders over 20 minutes
      preAllocatedVUs: 50,
      maxVUs: 300,
    },
  },
};
export default function () {
  const payload = JSON.stringify({
    userId: Math.floor(Math.random() * 1000),
    courseId: Math.floor(Math.random() * 200),
    amount: 99.99,
  });
  const res = http.post('http://host.docker.internal:8081/api/order', payload, {
    headers: { 'Content-Type': 'application/json' },
  });
  check(res, {
    'status is 200': (r) => r.status === 200,
  });
  // Small pause so VUs don't hammer in a tight loop
  sleep(1);
}