<?php

declare(strict_types=1);

ini_set('display_errors', '0');

header('Content-Type: application/json; charset=utf-8');

/**
 * GET METHOD
 * 
 * Send a JSON response and stop the script.
 */
function sendJson(array $data, int $statusCode = 200): never
{
    http_response_code($statusCode);

    echo json_encode(
        $data,
        JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES
    );

    exit;
}

/**
 * Find a student using the student's ID.
 */
function getStudentById(array $students, int $studentId): ?array
{
    foreach ($students as $student) {
        if (isset($student['ID']) && (int) $student['ID'] === $studentId) {
            return $student;
        }
    }

    return null;
}

/*
|--------------------------------------------------------------------------
| 1. Allow GET requests only
|--------------------------------------------------------------------------
*/

$requestMethod = $_SERVER['REQUEST_METHOD'] ?? '';

if ($requestMethod !== 'GET') {
    header('Allow: GET');

    sendJson([
        'success' => false,
        'message' => 'Only GET requests are allowed.'
    ], 405);
}

/*
|--------------------------------------------------------------------------
| 2. Load students.json
|--------------------------------------------------------------------------
*/

$jsonFile = __DIR__ . '/Students.json';

if (!file_exists($jsonFile)) {
    sendJson([
        'success' => false,
        'message' => 'Students.json was not found.'
    ], 500);
}

$jsonContent = file_get_contents($jsonFile);

if ($jsonContent === false) {
    sendJson([
        'success' => false,
        'message' => 'Unable to read Students.json.'
    ], 500);
}

try {
    $jsonData = json_decode(
        $jsonContent,
        true,
        512,
        JSON_THROW_ON_ERROR
    );
} catch (JsonException $exception) {
    sendJson([
        'success' => false,
        'message' => 'Students.json contains invalid JSON.'
    ], 500);
}

$students = $jsonData['students'] ?? null;

if (!is_array($students)) {
    sendJson([
        'success' => false,
        'message' => 'The students array is missing from students.json.'
    ], 500);
}

/*
|--------------------------------------------------------------------------
| 3. Get one student when ?id= is provided
|--------------------------------------------------------------------------
*/

if (isset($_GET['ID'])) {
    $studentId = filter_var(
        $_GET['ID'],
        FILTER_VALIDATE_INT,
        [
            'options' => [
                'min_range' => 1
            ]
        ]
    );

    if ($studentId === false) {
        sendJson([
            'success' => false,
            'message' => 'The student ID must be a positive integer.'
        ], 400);
    }

    $student = getStudentById($students, $studentId);

    if ($student === null) {
        sendJson([
            'success' => false,
            'message' => 'Student not found.'
        ], 404);
    }

    sendJson([
        'success' => true,
        'data' => $student
    ]);
}

/*
|--------------------------------------------------------------------------
| 4. Return all students
|--------------------------------------------------------------------------
*/

usort(
    $students,
    fn(array $first, array $second): int =>
        (int) ($first['ID'] ?? 0) <=> (int) ($second['ID'] ?? 0)
);

sendJson([
    'success' => true,
    'count' => count($students),
    'data' => $students
]);