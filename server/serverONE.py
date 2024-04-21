import socket
import pyaudio
import speech_recognition as sr


# server 1
def android_app_server():
    host = '192.168.188.20'
    port = 12345

    while True:
        p = pyaudio.PyAudio()
        vac_index = get_vac_index(p)
        print(vac_index)
        stream = p.open(format=pyaudio.paInt16, channels=1, rate=44100, output=True, output_device_index=vac_index)

        with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as s:
            s.bind((host, port))
            # s.listen(1) tcp
            # print("Server wartet auf Verbindung...") tcp

            # conn, addr = s.accept() tcp
            # print('Verbunden mit', addr) tcp
            addr = ""
            asdf = 0
            try:
                while True:
                    print("wait for data")
                    data = s.recv(16384)  # data = conn.recv(chunk)
                    asdf = asdf+1
                    # data = conn.recv(chunk)
                    print(data)
                    if not data:
                        break
                    print(f"data:", data)
                    # print_received_data(data)
                    stream.write(data)
                    if b"Disconnect" in data:
                        break
            except Exception as e:
                print("Fehler beim Empfangen von Daten:", e)
            finally:
                print('Schliese Verbindung mit', addr,asdf)
                # conn.close() tcp
                stream.stop_stream()
                stream.close()
                p.terminate()


def recognize_speech_from_stream(stream_data, sample_rate):
    recognizer = sr.Recognizer()
    audio_data = sr.AudioData(stream_data, sample_rate=sample_rate, sample_width=2)  # Annahme: 16-Bit PCM Audio
    try:
        text = recognizer.recognize_google(audio_data, language="de-DE")
        return text
    except sr.UnknownValueError:
        print("Die Spracherkennung konnte nichts erkennen.")
        return None
    except sr.RequestError as e:
        print("Fehler bei der Spracherkennung:", e)
        return None
    except AssertionError as e:
        return None


def print_received_data(data):
    print(recognize_speech_from_stream(data, sample_rate=44100))


def get_vac_index(p):
    info = p.get_host_api_info_by_index(0)
    numdevices = info.get('deviceCount')

    for i in range(0, numdevices):
        output = p.get_device_info_by_host_api_device_index(0, i)
        if (output.get('maxOutputChannels')) > 0 and output.get('name').__contains__("CABLE Input"):
            return i
    return -1


if __name__ == "__main__":
    android_app_server()
