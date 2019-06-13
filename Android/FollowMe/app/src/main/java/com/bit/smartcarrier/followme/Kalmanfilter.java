package com.bit.smartcarrier.followme;

//https://gyjmobile.tistory.com/entry/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C%EC%97%90%EC%84%9C%EC%9D%98-%EA%B0%80%EC%9E%A5-%EA%B0%84%EB%8B%A8%ED%95%98%EA%B3%A0-%ED%99%95%EC%8B%A4%ED%95%9C-%EC%B9%BC%EB%A7%8C%ED%95%84%ED%84%B0Kalman-Filter-%ED%85%8C%EC%8A%A4%ED%8A%B8
//칼만필터를 클래스로 선언한다. 여기에 쓰이는 공식은 이미 여러 사이트에 소개되어있다.
class Kalmanfilter {
    private double Q = 0.0005;
    private double R = 0.001;
    private double X, P = 1, K;

    //첫번째값을 입력받아 초기화 한다. 예전값들을 계산해서 현재값에 적용해야 하므로 반드시 하나이상의 값이 필요하므로~
    Kalmanfilter(double initValue)
    {
        X = initValue;
    }

    //예전값들을 공식으로 계산한다
    void measurementUpdate()
    {
        K = (P + Q) / (P + Q + R);
        P = R * (P + Q) / (R + P + Q);
    }

    //현재값을 받아 계산된 공식을 적용하고 반환한다
    double update(double measurement) {
        measurementUpdate();
        X = X + (measurement - X) * K;
        return X;
    }
}