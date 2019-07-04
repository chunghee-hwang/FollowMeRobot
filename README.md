# FollowMeRobot

#### 안드로이드 폰에서 라즈베리파이가 블루투스 목록에 뜨게 하는 과정
https://docs.google.com/document/d/1Bm8pNLWi0N6bJFj3h8M1D9kuVm6YiB5pHtsoO2CdqOs/edit?usp=sharing

### 통신 테스트 방법

#### 라즈베리파이 측

```
sudo apt-get update
sudo apt-get install python-pip3 python-dev ipython

sudo apt-get install bluetooth libbluetooth-dev
sudo pip3 install pybluez
```
python bluetoothRpi.py 실행

#### 안드로이드 측
블루투스 기기 목록에서  라즈베리파이 선택해서 페어링  
구글드라이브/안드로이드코드/apk-debug.apk 를 눌러서 앱을 설치  
	앱 실행  
	블루투스 목록 중 라즈베리파이 선택  
	rssi 값을 서로 통신하는 지 확인.


#### 참고 링크

안드로이드 코드(자바)  
https://webnautes.tistory.com/849  

안드로이드 코드에서 수정 사항 
(port 번호 수정)  
https://stackoverflow.com/questions/9703779/connecting-to-a-specific-bluetooth-port-on-a-bluetooth-device-using-android  

라즈베리파이 코드 (파이썬 코드)  
http://makeshare.org/bbs/board.php?bo_table=raspberrypi&wr_id=490  
