#!/usr/bin/expect
spawn sudo bluetoothctl
while 1 {
sleep 1
send "power on\r"
send "discoverable on\r"
send "pairable on\r"
send "agent NoInputNoOutput\r"
set timeout 1
expect "Agent registered"
send "default-agent\r"
set timeout 50
expect "Request authorization"
send "pairable off\r"
send "discoverable off\r"
send "power off\r"
sleep 10
}
