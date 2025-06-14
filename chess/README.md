A web-based chess game that I developed while I was learning WebSocket and other protocols. I updated it for the assignment and integrated a basic AI that uses minimax algorithm. It supports offline Player vs Player and Player vs AI (minimax) modes.

# How to Run

## **Clone the repository:**
    git clone https://github.com/berhudank/chess-ai.git

    cd chess-ai/chess

## **Start the spring boot application (JDK 17+ required):**
    ./mvnw spring-boot:run


## **Open your browser:**  
Go to [http://localhost:8080]

## **Play:**  
   - Choose your side (Black or White).
   - Open another browser tab and choose again to start playing.
   - Check "Play against AI" to play against computer.
   - To watch the flow of information between the server and client and see the game state changes, look at the console panel of the browser and the terminal that you used to run the application. I have not created any visual notifications yet. 

## **Note:** 
For now, 

En passant and castling are not supported and promotion options are limited to Queen. 

There is no time limit for moves. 

There is no fifty-move rule and threefold repetition rule.
