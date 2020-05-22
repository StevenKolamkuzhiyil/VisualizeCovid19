let canvas;
let tilemap;
let countries;
let geojson;
let info;
let lastLayer = null;
const grades = [0, 10, 20, 50, 100, 200, 500, 1000];

function preload() {}

function setup() {
  canvas = createCanvas(windowWidth, windowHeight - 116).parent("canvas-wrapper");

  createMap(48.210033, 16.363449);

  fill(70, 203, 31);
  stroke(100);
}

function draw() {
  noLoop();
}

/**
  * Resize canvas on window resize.
  */
function windowResized() {
  resizeCanvas(windowWidth, windowHeight - 116);
}

/**
  * Initialize the leaflet map and add it to canvas. Add labels, geojson data and control items
  * to the map and set click/touch events.
  */
function createMap(userLat, userLon) {
  tilemap = L.map("canvas-wrapper").setView([userLat, userLon], 4);
  tilemap.createPane("labels");
  tilemap.getPane("labels").style.zIndex = 650;
  tilemap.getPane("labels").style.pointerEvents = "none";

  L.tileLayer("https://{s}.basemaps.cartocdn.com/light_nolabels/{z}/{x}/{y}.png", {
    minZoom: 2,
    maxZoom: 8,
    zoomOffset: -1,
    tileSize: 512,
    attribution: "©OpenStreetMap, ©CartoDB",
  }).addTo(tilemap);

  L.tileLayer("https://{s}.basemaps.cartocdn.com/light_only_labels/{z}/{x}/{y}.png", {
    attribution: "©OpenStreetMap, ©CartoDB",
    pane: "labels",
  }).addTo(tilemap);

  geojson = L.geoJson(null, {
    style: style,
    onEachFeature: onEachLayer,
  }).addTo(tilemap);

  geojson.fire('data:loading');
  $.getJSON('/api/geojson', function (data) {
      geojson.fire('data:loaded');
      geojson.addData(data);
  });

  tilemap.touchZoom.disable();
  tilemap.doubleClickZoom.disable();
  tilemap.on("preclick", function (e) {
    reset();
  });

  L.control.scale().addTo(tilemap);

  info = L.control();
  info.onAdd = addDiv;
  info.update = updateDiv;
  info.addTo(tilemap);

  var legend = L.control({ position: "bottomright" });
  legend.onAdd = addLegend;
  legend.addTo(tilemap);

  tilemap.locate({ setView: false, maxZoom: 4 });
  tilemap.on("locationfound", onLocationFound);
}

function onLocationFound(e) {
  L.marker(e.latlng).addTo(tilemap).bindPopup("Your location");
  tilemap.flyTo(e.latlng, 4);
}

/**
  * Return color for the geojson layer.
  */
function getColor(cases) {
  return cases > grades[7] ? '#800026' :
         cases > grades[6] ? '#BD0026' :
         cases > grades[5] ? '#E31A1C' :
         cases > grades[4] ? '#FC4E2A' :
         cases > grades[3] ? '#FD8D3C' :
         cases > grades[2] ? '#FEB24C' :
         cases > grades[1] ? '#FED976' :
                             '#FFEDA0' ;
}

/**
  * Set style of the geojson layer.
  */
function style(feature) {
  const cases = parseInt(feature.properties.cases);
  return {
    fillColor: getColor(cases),
    weight: 3,
    opacity: 1,
    color: "white",
    dashArray: "3",
    fillOpacity: 0.7,
  };
}

/**
  * Get the layer the mouse is hovering over and highlight that layer.
  */
function highlightLayer(e) {
  var layer = e.target;
  if (lastLayer !== null && layer !== lastLayer) geojson.resetStyle(lastLayer);
  lastLayer = layer;

  layer.setStyle({
    fillColor: "white",
    weight: 3,
    opacity: 1,
    color: "#FFEDA0",
    dashArray: "3",
    fillOpacity: 0.7,
  });

  info.update(layer.feature.properties);

  if (!L.Browser.ie && !L.Browser.opera && !L.Browser.edge) {
    layer.bringToFront();
  }
}

/**
  * Reset the layer style and reset the info box.
  */
function resetHighlight(e) {
  geojson.resetStyle(e.target);
  info.update();
}
function reset() {
  if (lastLayer !== null) geojson.resetStyle(lastLayer);
  info.update();
}

/**
  * Add event listeners to each layer on the map.
  */
function onEachLayer(feature, layer) {
  layer.on({
    mouseover: highlightLayer,
    mouseout: resetHighlight,
    click: highlightLayer,
  });
}

/**
  * Add an info box to the map.
  */
function addDiv(tilemap) {
  this._div = L.DomUtil.create("div", "info");
  this.update();
  return this._div;
}

/**
  * Set the info box content.
  */
function updateDiv(props) {
  this._div.innerHTML = "<h6>COVID-19 Details</h6>" + (props ? createTableFromProps(props) : "Hover over a country");
}

/**
  * return a table created from a layers properties.
  */
function createTableFromProps(properties) {
  return "<table class='table table-sm table-borderless'><tbody>" + 
         "<tr><th>Country Code</th><td>" + (properties.geoId || "")                   + "</td>" +
         "<tr><th>Country Name</th><td>" + (properties.countriesAndTerritories || "") + "</td>" +
         "<tr><th>Population</th><td>"   + (properties.popData2018 || "")             + "</td>" +
         "<tr><th>Data of</th><td>"      + (properties.dateRep || "")                 + "</td>" +
         "<tr><th>New Cases</th><td>"    + (properties.cases || "")                   + "</td>" +
         "<tr><th>New Deaths</th><td>"   + (properties.deaths || "")                  + "</td>" +
         "</tbody></table>";
}

/**
  * Add a legend to the map.
  */
function addLegend(tilemap) {
  var div = L.DomUtil.create("div", "info legend"),
    labels = [];

  // loop through the COVID-19 case intervals and generate a label with a colored square for each interval
  for (var i = 0; i < grades.length; i++) {
    div.innerHTML +=
      '<i style="background:' +
      getColor(grades[i] + 1) +
      '"></i> ' +
      grades[i] +
      (grades[i + 1] ? "&ndash;" + grades[i + 1] + "<br>" : "+");
  }

  return div;
}
