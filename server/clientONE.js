let stream;
let websocket;
let mediaRecorder;

const startRecordButton = document.getElementById('startRecord');
const stopRecordButton = document.getElementById('stopRecord');

const playback = document.getElementById('playback')
const playback1 = document.getElementById('playback1')

startRecordButton.addEventListener('click', startRecording);
stopRecordButton.addEventListener('click', stopRecording);

function startRecording() {
    console.log("connect to websocket server!")
    websocket = new WebSocket('ws://localhost:8765');

    websocket.addEventListener('open', () => {
        console.log('Connected to WebSocket server.');
    });

    websocket.addEventListener('close', () => {
      console.log('Disconnected from WebSocket server.');
    });

    websocket.addEventListener('error', (error) => {
      console.error('WebSocket error:', error);
    });

    if(navigator.mediaDevices && navigator.mediaDevices.getUserMedia){
        navigator.mediaDevices
            .getUserMedia(
                {audio: true}
            )
            .then(streamParam => {
                console.log(streamParam)
                stream = streamParam;
                const options = {
                    mimeType:"audio/ogg; codecs=opus"//
                }
                playback.srcObject = stream;
                mediaRecorder = new MediaRecorder(stream, options);
                mediaRecorder.ondataavailable = (event) => {
                    console.log('ondataavailable', new Date())
                    if (event.data && event.data.size > 0) {
                        playback1.src = window.URL.createObjectURL(new Blob([event.data], {type: "audio/ogg; codecs=opus"}));
                        websocket.send(event.data);
                    }
                };
            mediaRecorder.start(5000);
            })
            .then()
            .catch(err => {
                console.log(err)
            });
    }
}
function stopRecording() {
    playback.srcObject = null;
    stream = null;
    if(websocket){
        websocket.close();
    }
    if(mediaRecorder){
        mediaRecorder.stop()
    }
}
