-- db/changelog/changes/004-insert-rental-spots.sql

-- liquibase formatted sql

-- changeset albert:1-insert-rental-spots
INSERT INTO rent_spots VALUES
    (1, NULL, 'Moscow', ST_GeomFromGeoJSON('{
  "coordinates": [
    [
      [
        37.4340302,
        55.8333008
      ],
      [
        37.4408966,
        55.7166685
      ],
      [
        37.5741059,
        55.6237315
      ],
      [
        37.7787262,
        55.6524106
      ],
      [
        37.8144318,
        55.7468255
      ],
      [
        37.7389008,
        55.8356144
      ],
      [
        37.5603729,
        55.8672207
      ],
      [
        37.5221756,
        55.7767661
      ],
      [
        37.4340302,
        55.8333008
      ]
    ]
  ],
  "type": "Polygon"
}')::geography, false),
    (2, 1, 'Moscow City', ST_GeomFromGeoJSON('{
  "coordinates": [
    [
      [
        37.5325605,
        55.7556313
      ],
      [
        37.5314447,
        55.7524193
      ],
      [
        37.5338023,
        55.7458696
      ],
      [
        37.5372916,
        55.7458447
      ],
      [
        37.5407573,
        55.746695
      ],
      [
        37.5504991,
        55.7510185
      ],
      [
        37.5472375,
        55.7539408
      ],
      [
        37.5486108,
        55.7558004
      ],
      [
        37.5459071,
        55.7574183
      ],
      [
        37.5367662,
        55.7554381
      ],
      [
        37.5325605,
        55.7556313
      ]
    ]
  ],
  "type": "Polygon"
}')::geography, false),
    (3, 2, 'Bus stop "Expocentr"', ST_GeomFromGeoJSON('{
  "coordinates": [
    [
      [
        37.5469387,
        55.7494642
      ],
      [
        37.5468019,
        55.7493932
      ],
      [
        37.5468851,
        55.7493434
      ],
      [
        37.5471694,
        55.7494914
      ],
      [
        37.5470996,
        55.7495321
      ],
      [
        37.5469387,
        55.7494642
      ]
    ]
  ],
  "type": "Polygon"
}')::geography, true),
    (4, 2, 'Bus stop "Metro Delovoy Centr"', ST_GeomFromGeoJSON('{
  "coordinates": [
    [
      [
        37.5410801,
        55.7524512
      ],
      [
        37.5411215,
        55.7524068
      ],
      [
        37.5412985,
        55.7524491
      ],
      [
        37.5412502,
        55.7525019
      ],
      [
        37.5410801,
        55.7524512
      ]
    ]
  ],
  "type": "Polygon"
}')::geography, true);
-- rollback DELETE FROM rent_spots;




