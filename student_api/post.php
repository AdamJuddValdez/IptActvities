<?php

declare(strict_types=1);

ini_set('strict_types', '0');

header('Content-Type: application/json; charset=utf-8');

function sendJson(array $data, int $statusCode = 200): never
{
    http_response_code($statusCode);

    echo json_encode(
        $data, 
        JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    header('Allow: POST');
    sendJson([
        'success' => false,
        'message' => 'Only POST requests are allowed.'
    ], 405);
}

$input = json_decode(file_get_contents('php://input'), true);

if (!is_array($input)) {
    sendJson([
        'success' => false,
        'message' => 'Invalid JSON data.'
    ], 400);
}

if (empty($input['firstname']) || empty($input['lastname'])) {
    sendJson([
        'success' => false,
        'message' => 'firstname and lastname are required.'
    ], 400);
}

$jsonFile = __DIR__ . '/Students.json';

if (!file_exists($jsonFile)) {
    sendJson([
        'success' => false,
        'message' => 'Students.json not found.'
    ], 500);
}

$jsonData = json_decode(file_get_contents($jsonFile), true);

if (!isset($jsonData['students'])) {
    $jsonData['students'] = [];
}

$students = $jsonData['students'];

$newId = 1;

if (!empty($students)) {
    $ids = array_column($students, 'ID');
    $newId = max(array_map('intval', $ids)) + 1;
}

$newStudent = [
    'ID' => (string)$newId,
    'firstname' => trim($input['firstname']),
    'lastname' => trim($input['lastname'])
];

$jsonData['students'][] = $newStudent;

file_put_contents(
    $jsonFile,
    json_encode($jsonData, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES)
);

sendJson([
    'success' => true,
    'message' => 'Student added successfully.',
    'data' => $newStudent
], 201);