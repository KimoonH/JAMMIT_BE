import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '20s', target: 100 },
        { duration: '20s', target: 200 },
        { duration: '20s', target: 300 },
        { duration: '20s', target: 500 },
        { duration: '20s', target: 700 },
        { duration: '20s', target: 1000 },
        { duration: '30s', target: 0 }, // 부하 제거
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'], // 95%가 500ms 이내면 통과
        http_req_failed: ['rate<0.01'],   // 실패율 1% 미만이면 통과
    },
};

export default function () {
    const res = http.get('http://localhost:8080/jammit/gatherings');
    check(res, {
        'status is 200': (r) => r.status === 200,
    });
    sleep(1);
}