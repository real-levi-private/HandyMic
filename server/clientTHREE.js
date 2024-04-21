const peerConnection = new RTCPeerConnection();

navigator.mediaDevices.getUserMedia({ audio: true })
    .then(stream => {
        stream.getTracks().forEach(track => {
            console.log("add stream to connection")
            peerConnection.addTrack(track, stream);
        });
    })
    .catch(error => {
        console.error('Error accessing media devices:', error);
    });

peerConnection.onicecandidate = event => {
    if (event.candidate) {
        console.log(event.candidate)
    }
};

peerConnection.onnegotiationneeded = async () => {
    try {
        await peerConnection.setLocalDescription(await peerConnection.createOffer());
    } catch (error) {
        console.error('Error creating offer:', error);
    }
};

peerConnection.ontrack = event => {
    console.log('Received remote track');
};

const ws = new WebSocket('ws://localhost:8080');

ws.onopen = () => {
    console.log('Connected to signaling server');
};

ws.onmessage = async event => {
    console.log("create peer connection")
    const message = JSON.parse(event.data);
    if (message.type === 'offer') {
        await peerConnection.setRemoteDescription(new RTCSessionDescription(message));
        const answer = await peerConnection.createAnswer();
        await peerConnection.setLocalDescription(answer);
    } else if (message.type === 'candidate') {
        await peerConnection.addIceCandidate(new RTCIceCandidate(message.candidate));
    }
};