const alice = new RTCPeerConnection();
const bob = new RTCPeerConnection();

alice.onicecandidate = e => {
    if(e.candidate){
        bob.addIceCandidate(e.candidate).then(r => console.log("promise bob"));
    }
}

bob.onicecandidate = e => {
    if(e.candidate){
        alice.addIceCandidate(e.candidate).then(r => console.log("promise alice"));
    }
}

navigator.mediaDevices.getUserMedia({audio:true})
    .then(stream => {
        //TODO document.getElementById("playback").srcObject = stream;
        alice.addTrack(stream.getTracks()[0]);
        return alice.createOffer();
    })
    .then(offer => alice.setLocalDescription(new RTCSessionDescription(offer)))
    .then(() => bob.setRemoteDescription((alice.localDescription)))
    .then(() => bob.createAnswer())
    .then(answer => bob.setLocalDescription(new RTCSessionDescription(answer)))
    .then(() => alice.setRemoteDescription(bob.localDescription));

bob.ontrack = e => {
    console.log("bob.ontrack:")
    console.log(e)
    //TODO document.getElementById("playback").srcObject = e.streams[0];
}