import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '2m', target: 200 },   // 0 → 200명 (워밍업)
        { duration: '2m', target: 500 },   // 200 → 500명 (중간 부하)
        { duration: '2m', target: 800 },   // 500 → 800명 (고부하)
        { duration: '2m', target: 1000 },  // 800 → 1000명 (최대 부하)
        { duration: '2m', target: 0 },     // 부하 제거
    ],
};

export default function () {
    const res = http.get('http://localhost:8083/jammit/gatherings');

    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    sleep(1); // 사용자 요청 간 딜레이
}