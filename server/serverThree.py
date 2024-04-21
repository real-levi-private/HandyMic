import asyncio
import json

import websockets

connected_clients = set()


async def handle_client(websocket, path):
    print("Client connected")
    try:
        async for message in websocket:
            print("Received message from client:", message)
            offer_message = {
                "type": "offer",
                "sdp": "example_sdp_offer_string"
            }
            await websocket.send(json.dumps(offer_message))
    except websockets.exceptions.ConnectionClosedError:
        print("Client disconnected")


# Funktion zum Senden einer Nachricht an alle verbundenen Clients
async def send_message_to_all(message):
    for client in connected_clients:
        await client.send(message)


async def main():
    server = await websockets.serve(handle_client, 'localhost', 8080)
    print("Server started, listening on ws://localhost:8080")

    await server.wait_closed()


asyncio.run(main())
