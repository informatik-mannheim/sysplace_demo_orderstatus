// -----------------------------------------------
// Controlling the tangible car over the internet
// -----------------------------------------------


int dig0 = D0;       // relais on digital out 0 
int dig2 = D2;       // LED headlights of the car 


void setup()
{
   // Pin configuration
   pinMode(dig0, OUTPUT);
   pinMode(dig2, OUTPUT);
    
   // This is saying that when we ask the cloud for the function "led", it will employ the function relaisToggle() from this app.
   Particle.function("led",relaisToggle);
   
   // Set Pins to LOW when it starts 
   digitalWrite(dig0, LOW);
   digitalWrite(dig2, LOW);

}

int relaisToggle(String command) {

    if (command=="0x20") {
        digitalWrite(dig2,HIGH);
        return 1;
        }
        
    if(command=="0x0"){
        digitalWrite(dig2, LOW);
        return 2;
        }

    if(command=="D0"){
        digitalWrite(dig0, HIGH);
        return 3;
        }
        
    if (command=="None") {
        digitalWrite(dig0,LOW);
        digitalWrite(dig2,LOW);
        return 0;
        }
  
    else {
        return -2;
    }
}