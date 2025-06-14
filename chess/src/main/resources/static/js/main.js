$(document).ready(function () {

    let connected = false
    let orientation = null
    let board = null

    let config = {
        onDrop: onDrop,
        onDragStart: onDragStart,
        snapSpeed: 500,
        showErrors: 'alert',
        draggable: true,
        dropOffBoard: 'snapback', // this is the default
        position: 'start',
    }

    const stompClient = new StompJs.Client({
        brokerURL: 'ws://localhost:8080/chess-websocket'
    });

    stompClient.onConnect = (frame) => {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/user/queue/newpos', (message) => {
            let msgbody = JSON.parse(message.body);
            console.log(msgbody)
            if (msgbody["response"] !== "user_exists") {
                $("body").append("<div id='board1' style='width: 400px'></div>")
                config["orientation"] = msgbody["orientation"]
                board = Chessboard('board1', config)
            }
            setTimeout(board.position(JSON.parse(msgbody["pos"])), 1000)
        });
        console.log("orientation when onConnect" + orientation)
        
        const aiChecked = window.aiCheckedGlobal || false;
        const destination = aiChecked ? "/app/addUserAI" : "/app/addUser";
        stompClient.publish({
            destination: destination,
            body: JSON.stringify({
                orientation: orientation,
                pos: "",
                response: ""
            })
        });
        stompClient.subscribe('/user/queue/pos', (message) => {
            let msgbody = JSON.parse(message.body);
            console.log(msgbody)
            setTimeout(board.position(JSON.parse(msgbody["pos"])), 1000)
        });
        stompClient.subscribe('/user/queue/gameState', (message) => {
            let msgbody = JSON.parse(message.body);
            console.log(msgbody)
            if (msgbody["response"] === "OPPONENT_LEFT") {
                console.log("Opponent has left the game")
                $("#board1").remove()
                stompClient.deactivate()
                console.log("Connection closed")
                connected = false;
            }
            if (msgbody["response"] === "NOT_A_USER") {
                console.log("Could not found the user")
                $("#board1").remove()
                stompClient.deactivate()
                console.log("Connection closed")
                connected = false;
            }

        });
        stompClient.subscribe('/user/queue/disconnect', () => {
            stompClient.deactivate()
            console.log("Connection closed")
            connected = false;
        });
        connected = true
    };

    stompClient.onWebSocketError = (error) => {
        console.error('Error with websocket', error);
    };

    stompClient.onStompError = (frame) => {
        console.error('Broker reported error: ' + frame.headers['message']);
        console.error('Additional details: ' + frame.body);
    };

    function onDrop(source, target, piece, newPos, oldPos, orientation) {

        if (target === 'offboard' || !connected)
            return 'snapback'

        stompClient.publish({
            destination: "/app/validate",
            body: JSON.stringify({
                source: source,
                target: target,
                piece: piece,
                oldPos: JSON.stringify(oldPos),
                orientation: orientation
            })
        });
    }

    function onDragStart(source, piece, position, orientation) {
        if ((orientation === 'white' && piece.search(/^w/) === -1) ||
            (orientation === 'black' && piece.search(/^b/) === -1)) {
            return false
        }
    }

    // $("button").click(() => {
    //     orientation = $(this).attr("id")
    //     console.log("button clicked: " + orientation)
    //     stompClient.activate()
    // });

    const buttons = document.querySelectorAll("button")
    console.log(buttons)
    buttons.forEach(element => {
        element.addEventListener("click", (e) => {
            orientation = e.target.attributes.getNamedItem("id").value
            console.log("button clicked: " + orientation)
            // Store AI checkbox state globally for use in onConnect
            window.aiCheckedGlobal = document.getElementById('ai-checkbox')?.checked || false;
            $("button").remove();
            $("p").remove();
            $("label").remove(); 
            stompClient.activate()
        })
    });

});