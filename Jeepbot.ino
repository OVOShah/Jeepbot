char blueToothVal;
const int motor1 = 11;
const int motor2 = 10;
const int turn1 = 7;
const int turn2 = 6;
const int trigPin = 4;
const int echoPin = 3;
const int red = 2;
const int white = 12;

 
void setup()
{
 Serial.begin(9600); 
 pinMode(motor1,OUTPUT);
 pinMode(motor2,OUTPUT);
 pinMode(turn1,OUTPUT);
 pinMode(turn2,OUTPUT);
 pinMode(red,OUTPUT);
 pinMode(white,OUTPUT);
}
 
 
void loop()
{
  if(Serial.available()){
    blueToothVal=Serial.read();
  }
  
  readSensor();
  
  clearAll();
  
  if (blueToothVal=='B'){
    back();
  }
  else if (blueToothVal=='F'){
    forward();
  }
  else if (blueToothVal == 'R'){
    right();
  }
  else if (blueToothVal == 'L'){
    left();
  }
  else if (blueToothVal == 'G'){
    left();
    forward();
  }
  else if (blueToothVal == 'I'){
    forward();
    right();
  }
  else if (blueToothVal == 'H'){
    back();
    left();
  }
  else if (blueToothVal == 'J'){
    back();
    right();
  }
  else if (blueToothVal == 'W'){
    digitalWrite(white,HIGH);
  }
  else if (blueToothVal == 'w'){
    digitalWrite(white,LOW);
  }
  else{
    clearAll();
  }
  
  delay(100); 
}

void clearAll(){
    analogWrite(turn2,0);
    analogWrite(turn1,0);
    analogWrite(motor1,0);
    analogWrite(motor2,0);
    digitalWrite(red,LOW);
}

void left(){
  analogWrite(turn2,255);
}

void right(){
  analogWrite(turn1,255);
}

void forward(){
  analogWrite(motor2,255);
}

void back(){
  analogWrite(motor1,255);
  digitalWrite(red,HIGH);
}

//Reads ultrasonic sensor and backs up if too close to object
void readSensor(){
  long duration, inches, cm;
  
  pinMode(trigPin, OUTPUT);
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);
  
  pinMode(echoPin, INPUT);
  duration = pulseIn(echoPin, HIGH);
  
  inches = microsecondsToInches(duration);
  cm = microsecondsToCentimeters(duration);
  
  if (inches < 6)
  {
    clearAll();
    back();
    delay(300);
    clearAll();
  }
}

long microsecondsToInches(long microseconds)
{
  return microseconds / 74 / 2;
}
 
long microsecondsToCentimeters(long microseconds)
{
  return microseconds / 29 / 2;
}
