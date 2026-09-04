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

function validateStudentData(array $data): array
{
    $errors = [];

    if (empty($data['IDNumber']) || !is_string($data['IDNumber'])) {
        $errors[] = 'ID number is required and must be a string.';
    }

    if (empty($data['firstname']) || !is_string($data['firstname'])) {
        $errors[] = 'firstname is required and must be a string.';
    }

    if (empty($data['lastname']) || !is_string($data['lastname'])) {
        $errors[] = 'lastname is required and must be a string.';
    }

    if (empty($data['age']) || !is_numeric($data['age']) || (int)$data['age'] < 1 || (int)$data['age'] > 120) {
        $errors[] = 'Age is required and must be a positive number.';
    }

    if (empty($data['gender']) || !is_string($data['gender'])) {
        $errors[] = 'Gender is required and must be a string.';
    }

    if (empty($data['bloodType']) || !is_string($data['bloodType'])) {
        $errors[] = 'Blood type is required and must be a string.';
    }

    return $errors;
}

function getHighestStudentID(array $students): int
{
    $maxId = 0;

    foreach ($students as $student) {
        $id = (int) ($student['id'] ?? 0);
        if ($id > $maxId) {
            $maxId = $id;
        }
    }

    return $maxId;
}   


$requestMethod = $_SERVER['REQUEST_METHOD'] ?? '';

if($requestMethod !== 'POST') {
    header('Allow: POST');

    sendJson([
        'success' => false,
        'message' => 'Only POST requests are allowed.'
    ], 405);
}


$inputContent = file_get_contents('php://input');

if($inputContent === false) {
    sendJson([
        'success' => false,
        'message' => 'Unable to read input data.'
    ], 400);
}

try {
    $inputData = json_decode(
        $inputContent, 
        true, 
        512, 
        JSON_THROW_ON_ERROR);
} catch (JsonException $e) {
    sendJson([
        'success' => false,
        'message' => 'Invalid JSON input: ' . $e->getMessage()
    ], 400);
}

$errors = validateStudentData($inputData);

if(!empty($errors)) {
    sendJson([
        'success' => false,
        'message' => 'Validation errors occurred.',
        'errors' => $errors
    ], 400);
}

$jsonFile = __DIR__ . '/Students.json';

if(!file_exists($jsonFile)) {
    sendJson([
        'success' => false,
        'message' => 'Students.json was not found.'
    ], 500);
}

$jsonContent = file_get_contents($jsonFile);

if($jsonContent === false) {
    sendJson([
        'success' => false,
        'message' => 'Unable to read Students.json.'
    ], 500);
}

try {
    $studentsData = json_decode(
        $jsonContent, 
        true, 
        512, 
        JSON_THROW_ON_ERROR);
} catch (JsonException $e) {
    sendJson([
        'success' => false,
        'message' => 'Students.json contains invalid JSON.'
    ], 500);
}

$students = $jsonData['students'] ?? [];

if(!is_array($students)) {
    sendJson([
        'success' => false,
        'message' => 'Invalid students data structure in Students.json.'
    ], 500);
}

$newStudent = [
    'id' => getHighestStudentID($students) + 1,
    'IDNumber' => trim($inputData['IDNumber']),
    'firstname' => trim($inputData['firstname']),
    'lastname' => trim($inputData['lastname']),
    'age' => (int) $inputData['age'],
    'gender' => trim($inputData['gender']),
    'bloodType' => trim($inputData['bloodType'])

];


$students[] = $newStudent;
$jsonData['students'] = $students;

$updatedJson = json_encode(
    $jsonData,
    JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES
);

if($updatedJson === false) {
    sendJson([
        'success' => false,
        'message' => 'Failed to encode updated students data to JSON.'
    ], 500);
}

sendJson([
    'success' => true,
    'message' => 'Student added successfully.',
    'student' => $newStudent
], 201);