import pygame.midi
import asyncio
import websockets
import json
import threading

# WebSocket address of your Minecraft plugin
# Open your port 3000 on your router for the WebSocket server.
# Online Minecraft server. Use the public IP of your Minecraft server. Example with port 3000: ws://111.111.11.11:3000
WS_URI = "ws://111.111.11.11:3000"
minecraft_pseudo = "your_username"
midi_device_name_substring = "Your Device Name"  # Search for the port that contains the device name (you can find it with the ListDevices.py script)

# Map MIDI numbers to instrument names the plugin expects
# General MIDI standard
NOTE_MAP = {
    36: "kick",
    38: "snare",
    40: "side_stick",
    43: "tom_low",
    47: "tom_medium",
    50: "tom_high",
    22: "hi_hat_foot",
    49: "crash_1",
    51: "ride_bow",
    53: "ride_bell",
    57: "crash_2",
    59: "ride_edge",
}

last_cc4_value = 127  # 0 = hi-hat open, 127 = hi-hat closed

def get_hi_hat_state(cc_value):
    if cc_value > 95:
        return "hi_hat_tight"
    elif cc_value > 25:
        return "hi_hat_loose"
    else:
        return "hi_hat_open"

async def envoyer(websocket, data):
    await websocket.send(json.dumps(data))
    # print(f"Sent: {data}")

def find_midi_input_id(name_substring):
    for i in range(pygame.midi.get_count()):
        info = pygame.midi.get_device_info(i)
        # info = (interface, name, is_input, is_output, opened)
        if info[2] == 1 and name_substring.encode() in info[1]:
            return i
    return None

def midi_reader(loop, websocket, input_id):
    global last_cc4_value
    midi_in = pygame.midi.Input(input_id)
    try:
        while True:
            if midi_in.poll():
                events = midi_in.read(1) # Read the event as soon as it is available
                for event in events:
                    data, timestamp = event
                    status = data[0] & 0xF0
                    channel = data[0] & 0x0F
                    note = data[1]
                    velocity = data[2]

                    if status == 0xB0: # Control change
                        if note == 4:
                            last_cc4_value = velocity
                    elif status == 0x90 and velocity > 0: # Note on
                        if note == 22:
                            asyncio.run_coroutine_threadsafe(
                                envoyer(websocket, {"note": "hi_hat_foot", "velocity": velocity}), loop
                            )
                        elif note in [42, 46, 8]:
                            note_name = get_hi_hat_state(last_cc4_value)
                            asyncio.run_coroutine_threadsafe(
                                envoyer(websocket, {"note": note_name, "velocity": velocity}), loop
                            )
                        elif note in NOTE_MAP:
                            asyncio.run_coroutine_threadsafe(
                                envoyer(websocket, {"note": NOTE_MAP[note], "velocity": velocity}), loop
                            )
            else:
                pygame.time.wait(0)
    finally:
        midi_in.close()

async def main():
    pygame.midi.init()
    input_id = find_midi_input_id(midi_device_name_substring)
    if input_id is None:
        print("MIDI device not found")
        return

    async with websockets.connect(WS_URI, ping_interval=20, ping_timeout=20, compression=None) as websocket:
        await envoyer(websocket, {"player": minecraft_pseudo})
        loop = asyncio.get_running_loop()
        threading.Thread(target=midi_reader, args=(loop, websocket, input_id), daemon=True).start()
        await asyncio.Future() # Keeping Asyncio Alive

if __name__ == "__main__":
    asyncio.run(main())