from setuptools import setup, find_packages

setup(
    name="csintegration",
    version="1.0.0",
    description="CloudStack Integration Framework — plugin-based platform integration",
    long_description=open("README.md").read(),
    long_description_content_type="text/markdown",
    author="CloudStack Integration Framework Contributors",
    license="Apache-2.0",
    packages=find_packages(exclude=["tests*"]),
    python_requires=">=3.10",
    install_requires=[
        "fastapi>=0.109.0",
        "uvicorn[standard]>=0.27.0",
        "httpx>=0.26.0",
        "pydantic>=2.5.0",
        "pyyaml>=6.0.1",
    ],
    extras_require={
        "kafka": ["aiokafka>=0.10.0"],
        "rabbitmq": ["aio-pika>=9.4.0"],
        "kubernetes": ["kubernetes-asyncio>=30.0"],
        "dev": [
            "pytest>=8.0.0",
            "pytest-asyncio>=0.23.0",
        ],
    },
    entry_points={
        "console_scripts": [
            "csintegration=csintegration.main:main",
        ],
    },
    classifiers=[
        "Development Status :: 4 - Beta",
        "Framework :: FastAPI",
        "Intended Audience :: Developers",
        "Intended Audience :: System Administrators",
        "License :: OSI Approved :: Apache Software License",
        "Programming Language :: Python :: 3.10",
        "Programming Language :: Python :: 3.11",
        "Programming Language :: Python :: 3.12",
        "Topic :: System :: Systems Administration",
    ],
)
