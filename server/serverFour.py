import asyncio
from websockets.server import serve


async def echo(websocket):
    async for message in websocket:
        print(message)
        # await websocket.send(message)


async def main():
    async with serve(echo, "localhost", 8765):
        await asyncio.Future()


if __name__ == "__main__":
    asyncio.run(main())
