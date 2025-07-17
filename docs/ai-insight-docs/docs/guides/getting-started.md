---
sidebar_position: 1
---

# Getting Started

This guide will help you get started with the AI Insight Dashboard.

## Prerequisites

Before you begin, make sure you have the following installed:

- Node.js (v14 or later)
- npm or yarn
- Java Development Kit (JDK) 11 or later

## Installation

### Step 1: Clone the repository

```bash
git clone https://github.com/UNBunny/ai-insight-dashboard.git
cd ai-insight-dashboard
```

### Step 2: Install frontend dependencies

```bash
cd client
npm install
```

### Step 3: Install backend dependencies

The backend uses Maven for dependency management:

```bash
cd ..  # Return to project root
mvn install
```

## Running the Application

### Start the backend server

```bash
mvn spring-boot:run
```

The server will start at http://localhost:8081

### Start the frontend development server

```bash
cd client
npm start
```

The frontend development server will start at http://localhost:3000

## First Steps

1. **Log in to the dashboard**: Navigate to http://localhost:3000 and log in with default credentials (admin/password).
2. **Explore the dashboard**: Familiarize yourself with the layout and available metrics.
3. **Connect your data sources**: Configure connections to your AI systems to start gathering metrics.
4. **Create your first visualization**: Follow the [Creating Visualizations](/docs/guides/creating-visualizations) guide.

## Next Steps

Once you're up and running, check out these resources:

- [Dashboard Customization](/docs/features/dashboard)
- [Working with Metrics](/docs/features/metrics)
- [Analytics Tools](/docs/features/analytics)
- [API Integration](/docs/api/overview)
