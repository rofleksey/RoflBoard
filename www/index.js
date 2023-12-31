Alpine.store('connected', false);
Alpine.store('sounds', []);

navigator.wakeLock?.request("screen").catch((e) => {
  console.error(e);
})

let ws

function initApp() {
  try {
    console.log(`connecting to ws://${window.location.host}/ws`);
    ws = new WebSocket(`ws://${window.location.host}/ws`);

    ws.onopen = function(e) {
      Alpine.store('connected', true);
      console.log('ws connected');
    };

    ws.onmessage = function(event) {
      const { type, data } = JSON.parse(event.data);
      if (type === "list") {
        Alpine.store('sounds', data);
        console.log(data);
      }
    };

    ws.onclose = function(event) {
      Alpine.store('connected', false);
      console.log('ws closed');
      setTimeout(initApp, 1000);
    };

    ws.onerror = function(error) {
      Alpine.store('connected', false);
      console.error("ws error: ", error);
    };
  } catch(e) {
    console.error(e);
    setTimeout(initApp, 1000);
  }
}

function sendWsMessage(data) {
  try {
    ws?.send(JSON.stringify(data));
  } catch(ignored) {
  }
}

function onSoundMouseDown(sound) {
  console.log(`starting sound ${sound.name}`);
  sendWsMessage({
    type: "start",
    data: sound.id,
  });
}

function onSoundMouseUp(sound) {
  console.log(`stopping sound ${sound.name}`);
  sendWsMessage({
    type: "stop",
    data: sound.id,
  });
}