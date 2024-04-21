import asyncio
import pyaudio
import websockets


# server 2
async def audio_stream(websocket):
    print("Client verbunden.")
    p = pyaudio.PyAudio()
    stream = p.open(format=pyaudio.paInt16, channels=1, rate=44100, output=True)
    try:
        while True:
            audio_data = await websocket.recv()
            print("Empfangene Daten: " + str(audio_data))
            stream.write(audio_data)
    except websockets.exceptions.ConnectionClosedError:
        print("Verbindung zum Client geschlossen. ConnectionClosedError")
    except websockets.exceptions.ConnectionClosedOK:
        print("Verbindung zum Client geschlossen. ConnectionClosedOK")
    finally:
        stream.stop_stream()
        stream.close()
        p.terminate()


async def run_server():
    start_server = await websockets.serve(audio_stream, "localhost", 8765)
    print("WebSocket-Server gestartet.")

    # Warte, bis der Server beendet wird
    await start_server.wait_closed()


asyncio.run(run_server())
